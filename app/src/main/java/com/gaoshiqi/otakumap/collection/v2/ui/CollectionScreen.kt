package com.gaoshiqi.otakumap.collection.v2.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gaoshiqi.otakumap.R
import com.gaoshiqi.otakumap.collection.v2.viewmodel.CollectionIntent
import com.gaoshiqi.otakumap.collection.v2.viewmodel.CollectionState
import com.gaoshiqi.otakumap.collection.v2.viewmodel.STATUS_ALL
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CollectionScreen(
    state: CollectionState,
    onIntent: (CollectionIntent) -> Unit,
    onClearError: () -> Unit,
    onBackClick: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = state.currentTabIndex,
        pageCount = { state.tabs.size }
    )
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // 同步 Tab 和 Pager
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != state.currentTabIndex) {
            onIntent(CollectionIntent.SwitchTab(pagerState.currentPage))
        }
    }

    LaunchedEffect(state.currentTabIndex) {
        if (pagerState.currentPage != state.currentTabIndex) {
            pagerState.animateScrollToPage(state.currentTabIndex)
        }
    }

    // 错误提示
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onClearError()
        }
    }

    // 排序菜单状态
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.collection_v2_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Sort"
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.sort_by_time)) },
                                onClick = {
                                    onIntent(CollectionIntent.ToggleSortOrder(sortByName = false))
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.sort_by_name)) },
                                onClick = {
                                    onIntent(CollectionIntent.ToggleSortOrder(sortByName = true))
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab 栏
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth()
            ) {
                state.tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(stringResource(tab.titleResId)) }
                    )
                }
            }

            // 内容区域（ViewPager）
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val tabStatus = state.tabs[page].status
                val isAllTab = tabStatus == STATUS_ALL
                val animeList = state.animeListByTab[tabStatus] ?: emptyList()

                if (animeList.isEmpty()) {
                    // 使用通用空态组件
                    EmptyStateView(
                        message = stringResource(R.string.collection_empty_tab),
                        iconRes = R.mipmap.ic_loading_empty
                    )
                } else {
                    // 网格列表
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = animeList,
                            key = { it.id }
                        ) { anime ->
                            AnimeCard(
                                anime = anime,
                                onClick = {
                                    onIntent(CollectionIntent.NavigateToDetail(anime))
                                },
                                onStatusChange = { newStatus ->
                                    onIntent(CollectionIntent.UpdateStatus(anime.id, newStatus))
                                },
                                onRemove = {
                                    onIntent(CollectionIntent.RemoveAnime(anime.id))
                                },
                                showStatusBadge = isAllTab
                            )
                        }
                    }
                }
            }
        }
    }
}
