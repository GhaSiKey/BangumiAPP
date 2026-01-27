package com.gaoshiqi.player.omofun.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gaoshiqi.player.PlayerTestActivity
import com.gaoshiqi.player.ui.theme.PlayerTheme

/**
 * Omofun 功能入口 Activity
 * 使用 Navigation Compose 管理搜索页和详情页
 */
class OmofunActivity : ComponentActivity() {

    private lateinit var viewModel: OmofunViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[OmofunViewModel::class.java]

        setContent {
            PlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OmofunNavHost(
                        viewModel = viewModel,
                        onPlayVideo = { videoUrl, title, _ ->
                            PlayerTestActivity.start(this@OmofunActivity, videoUrl, title)
                        },
                        onFinish = { finish() }
                    )
                }
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, OmofunActivity::class.java)
            context.startActivity(intent)
        }
    }
}

/**
 * Navigation 路由定义
 */
sealed class OmofunRoute(val route: String) {
    data object Search : OmofunRoute("search")
    data object Detail : OmofunRoute("detail/{detailUrl}") {
        fun createRoute(detailUrl: String): String {
            return "detail/${Uri.encode(detailUrl)}"
        }
    }
}

@Composable
private fun OmofunNavHost(
    viewModel: OmofunViewModel,
    onPlayVideo: (videoUrl: String, title: String, referer: String?) -> Unit,
    onFinish: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    // 收集播放事件
    LaunchedEffect(Unit) {
        viewModel.playEvent.collect { event ->
            onPlayVideo(event.videoUrl, event.title, event.referer)
        }
    }

    NavHost(
        navController = navController,
        startDestination = OmofunRoute.Search.route
    ) {
        // 搜索页
        composable(OmofunRoute.Search.route) {
            val searchResults by viewModel.searchResults.collectAsState()
            val uiState by viewModel.uiState.collectAsState()

            OmofunSearchScreen(
                searchResults = searchResults,
                isLoading = uiState.isLoading,
                error = uiState.error,
                onSearch = { keyword -> viewModel.search(keyword) },
                onResultClick = { result ->
                    // 跳转详情页
                    navController.navigate(OmofunRoute.Detail.createRoute(result.detailUrl))
                },
                onBackClick = onFinish
            )
        }

        // 详情页
        composable(
            route = OmofunRoute.Detail.route,
            arguments = listOf(
                navArgument("detailUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val detailUrl = Uri.decode(backStackEntry.arguments?.getString("detailUrl") ?: "")
            val animeDetail by viewModel.animeDetail.collectAsState()
            val uiState by viewModel.uiState.collectAsState()

            // 加载详情
            LaunchedEffect(detailUrl) {
                if (detailUrl.isNotBlank()) {
                    viewModel.loadDetail(detailUrl)
                }
            }

            OmofunDetailScreen(
                detail = animeDetail,
                isLoading = uiState.isLoading,
                isExtracting = uiState.isExtracting,
                error = uiState.error,
                onEpisodeClick = { episode ->
                    viewModel.playEpisode(episode)
                },
                onBackClick = {
                    viewModel.clearDetail()
                    navController.popBackStack()
                },
                onErrorDismiss = {
                    viewModel.clearError()
                }
            )
        }
    }
}
