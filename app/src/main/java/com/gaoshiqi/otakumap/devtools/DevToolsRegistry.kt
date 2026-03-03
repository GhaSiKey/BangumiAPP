package com.gaoshiqi.otakumap.devtools

import android.app.Activity
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import com.gaoshiqi.camera.CameraActivity
import com.gaoshiqi.map.MapsDemoActivity
import com.gaoshiqi.otakumap.R
import com.gaoshiqi.otakumap.collection.MyCollectionActivity
import com.gaoshiqi.otakumap.collection.v2.CollectionV2Activity
import com.gaoshiqi.otakumap.comparison.SavedPointPickerActivity
import com.gaoshiqi.otakumap.demo.UiDemoActivity
import com.gaoshiqi.otakumap.schedule.BangumiTodayActivity
import com.gaoshiqi.otakumap.schedule.ScheduleActivity
import com.gaoshiqi.otakumap.search.SearchActivity
import com.gaoshiqi.otakumap.search.SearchOldTestActivity
import com.gaoshiqi.otakumap.search.SearchTestActivity
import com.gaoshiqi.otakumap.trending.AnimeTrendingActivity
import com.gaoshiqi.player.PlayerTestActivity

object DevToolsRegistry {

    val items: List<DevToolItem> = listOf(
        // ── Data / API ──────────────────────────────────
        DevToolItem(
            titleResId = R.string.devtools_today_schedule,
            descriptionResId = R.string.devtools_desc_today_schedule,
            icon = Icons.Default.DateRange,
            category = DevToolsCategory.DATA_API,
            action = { it.startActivity(Intent(it, BangumiTodayActivity::class.java)) },
        ),
        DevToolItem(
            titleResId = R.string.devtools_schedule,
            descriptionResId = R.string.devtools_desc_schedule,
            icon = Icons.AutoMirrored.Filled.List,
            category = DevToolsCategory.DATA_API,
            action = { it.startActivity(Intent(it, ScheduleActivity::class.java)) },
        ),
        DevToolItem(
            titleResId = R.string.devtools_collection,
            descriptionResId = R.string.devtools_desc_collection,
            icon = Icons.Default.Favorite,
            category = DevToolsCategory.DATA_API,
            action = { it.startActivity(Intent(it, MyCollectionActivity::class.java)) },
        ),
        DevToolItem(
            titleResId = R.string.devtools_rankings,
            descriptionResId = R.string.devtools_desc_rankings,
            icon = Icons.Default.Star,
            category = DevToolsCategory.DATA_API,
            action = { it.startActivity(Intent(it, AnimeTrendingActivity::class.java)) },
        ),
        DevToolItem(
            titleResId = R.string.devtools_search,
            descriptionResId = R.string.devtools_desc_search,
            icon = Icons.Default.Search,
            category = DevToolsCategory.DATA_API,
            action = { it.startActivity(Intent(it, SearchActivity::class.java)) },
        ),
        DevToolItem(
            titleResId = R.string.devtools_new_search,
            descriptionResId = R.string.devtools_desc_new_search,
            icon = Icons.Default.Refresh,
            category = DevToolsCategory.DATA_API,
            action = { SearchTestActivity.start(it) },
        ),
        DevToolItem(
            titleResId = R.string.devtools_old_search,
            descriptionResId = R.string.devtools_desc_old_search,
            icon = Icons.Default.Home,
            category = DevToolsCategory.DATA_API,
            action = { SearchOldTestActivity.start(it) },
        ),

        // ── UI ──────────────────────────────────────────
        DevToolItem(
            titleResId = R.string.devtools_player,
            descriptionResId = R.string.devtools_desc_player,
            icon = Icons.Default.PlayArrow,
            category = DevToolsCategory.UI,
            action = { PlayerTestActivity.start(it) },
        ),
        DevToolItem(
            titleResId = R.string.devtools_collection_v2,
            descriptionResId = R.string.devtools_desc_collection_v2,
            icon = Icons.Default.FavoriteBorder,
            category = DevToolsCategory.UI,
            action = { CollectionV2Activity.start(it) },
        ),
        DevToolItem(
            titleResId = R.string.devtools_ui_demo,
            descriptionResId = R.string.devtools_desc_ui_demo,
            icon = Icons.Default.Info,
            category = DevToolsCategory.UI,
            action = { UiDemoActivity.start(it) },
        ),

        // ── Hardware ────────────────────────────────────
        DevToolItem(
            titleResId = R.string.devtools_google_map,
            descriptionResId = R.string.devtools_desc_google_map,
            icon = Icons.Default.Place,
            category = DevToolsCategory.HARDWARE,
            action = { it.startActivity(Intent(it, MapsDemoActivity::class.java)) },
        ),
        DevToolItem(
            titleResId = R.string.devtools_camera,
            descriptionResId = R.string.devtools_desc_camera,
            icon = Icons.Default.Star,
            category = DevToolsCategory.HARDWARE,
            action = { CameraActivity.start(it) },
        ),
        DevToolItem(
            titleResId = R.string.devtools_comparison_camera,
            descriptionResId = R.string.devtools_desc_comparison_camera,
            icon = Icons.Default.Refresh,
            category = DevToolsCategory.HARDWARE,
            action = { SavedPointPickerActivity.start(it) },
        ),

        // ── Firebase ────────────────────────────────────
        DevToolItem(
            titleResId = R.string.devtools_crash_test,
            descriptionResId = R.string.devtools_desc_crash_test,
            icon = Icons.Default.Warning,
            category = DevToolsCategory.FIREBASE,
            requiresConfirmation = true,
            action = { throw RuntimeException("Firebase Crashlytics Test") },
        ),
        DevToolItem(
            titleResId = R.string.devtools_anr_test,
            descriptionResId = R.string.devtools_desc_anr_test,
            icon = Icons.Default.Warning,
            category = DevToolsCategory.FIREBASE,
            requiresConfirmation = true,
            action = {
                @Suppress("BlockingMethodInNonBlockingContext")
                Thread.sleep(10_000)
            },
        ),
    )

    fun getItemsByCategory(category: DevToolsCategory): List<DevToolItem> =
        items.filter { it.category == category }
}
