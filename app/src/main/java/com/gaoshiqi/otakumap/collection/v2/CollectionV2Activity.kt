package com.gaoshiqi.otakumap.collection.v2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.gaoshiqi.otakumap.collection.v2.ui.CollectionScreen
import com.gaoshiqi.otakumap.collection.v2.viewmodel.CollectionViewModel
import com.gaoshiqi.otakumap.detail.BangumiDetailActivity

class CollectionV2Activity : ComponentActivity() {

    private val viewModel: CollectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CollectionV2Theme {
                val state by viewModel.state.collectAsState()

                // 处理导航事件
                LaunchedEffect(state.navigateToDetail) {
                    state.navigateToDetail?.let { anime ->
                        BangumiDetailActivity.start(this@CollectionV2Activity, anime.id)
                        viewModel.onNavigatedToDetail()
                    }
                }

                CollectionScreen(
                    state = state,
                    onIntent = viewModel::processIntent,
                    onClearError = viewModel::clearError,
                    onBackClick = { finish() }
                )
            }
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, CollectionV2Activity::class.java))
        }
    }
}

@Composable
fun CollectionV2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme()
    } else {
        lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
