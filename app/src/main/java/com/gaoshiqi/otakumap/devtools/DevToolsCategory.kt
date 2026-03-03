package com.gaoshiqi.otakumap.devtools

import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.gaoshiqi.otakumap.R

enum class DevToolsCategory(@param:StringRes val labelResId: Int) {
    DATA_API(R.string.devtools_category_data_api),
    UI(R.string.devtools_category_ui),
    HARDWARE(R.string.devtools_category_hardware),
    FIREBASE(R.string.devtools_category_firebase),
}

data class DevToolItem(
    @param:StringRes val titleResId: Int,
    @param:StringRes val descriptionResId: Int,
    val icon: ImageVector,
    val category: DevToolsCategory,
    val requiresConfirmation: Boolean = false,
    val action: (Activity) -> Unit,
)
