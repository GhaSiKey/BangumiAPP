package com.gaoshiqi.otakumap.collection.v2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gaoshiqi.otakumap.R
import com.gaoshiqi.otakumap.collection.v2.ui.CollectionScreenContent
import com.gaoshiqi.otakumap.collection.v2.viewmodel.CollectionIntent
import com.gaoshiqi.otakumap.collection.v2.viewmodel.CollectionViewModel
import com.gaoshiqi.otakumap.detail.BangumiDetailActivity

/**
 * 新版收藏页 Fragment
 *
 * 将 CollectionV2Activity 的 Compose 内容封装为 Fragment，
 * 以适配 Navigation Component 和底部导航栏。
 */
class CollectionV2Fragment : Fragment() {

    private val viewModel: CollectionViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // 确保 Compose 与 Fragment 生命周期正确同步
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                CollectionV2Theme {
                    val state by viewModel.state.collectAsState()
                    val snackbarHostState = remember { SnackbarHostState() }

                    // 处理导航到详情页
                    LaunchedEffect(state.navigateToDetail) {
                        state.navigateToDetail?.let { anime ->
                            BangumiDetailActivity.start(requireContext(), anime.id)
                            viewModel.onNavigatedToDetail()
                        }
                    }

                    // 排序菜单状态
                    var showSortMenu by remember { mutableStateOf(false) }

                    Scaffold(
                        // 禁用 Scaffold 的 WindowInsets 处理，因为 Activity 已经处理了
                        contentWindowInsets = WindowInsets(0.dp),
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        text = stringResource(R.string.collection_v2_title),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                // 禁用 TopAppBar 的 WindowInsets 处理，避免双重 padding
                                windowInsets = WindowInsets(0.dp),
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
                                                    viewModel.processIntent(
                                                        CollectionIntent.ToggleSortOrder(sortByName = false)
                                                    )
                                                    showSortMenu = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text(stringResource(R.string.sort_by_name)) },
                                                onClick = {
                                                    viewModel.processIntent(
                                                        CollectionIntent.ToggleSortOrder(sortByName = true)
                                                    )
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
                        CollectionScreenContent(
                            state = state,
                            onIntent = viewModel::processIntent,
                            onClearError = viewModel::clearError,
                            snackbarHostState = snackbarHostState,
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                }
            }
        }
    }
}
