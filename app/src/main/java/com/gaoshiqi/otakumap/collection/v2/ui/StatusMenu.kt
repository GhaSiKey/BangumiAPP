package com.gaoshiqi.otakumap.collection.v2.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaoshiqi.otakumap.R
import com.gaoshiqi.room.AnimeEntity
import com.gaoshiqi.room.CollectionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusMenu(
    anime: AnimeEntity,
    onDismiss: () -> Unit,
    onStatusChange: (Int) -> Unit,
    onProgressChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var showRemoveDialog by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableIntStateOf(anime.collectionStatus) }
    var currentProgress by remember { mutableIntStateOf(anime.watchedEpisodes) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 标题
            Text(
                text = anime.displayName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 状态选择
            Text(
                text = stringResource(R.string.collection_status_label),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(Modifier.selectableGroup()) {
                StatusOption(
                    status = CollectionStatus.DOING,
                    label = stringResource(R.string.collection_doing),
                    isSelected = selectedStatus == CollectionStatus.DOING,
                    onSelect = {
                        selectedStatus = CollectionStatus.DOING
                        onStatusChange(CollectionStatus.DOING)
                    }
                )
                StatusOption(
                    status = CollectionStatus.WISH,
                    label = stringResource(R.string.collection_wish),
                    isSelected = selectedStatus == CollectionStatus.WISH,
                    onSelect = {
                        selectedStatus = CollectionStatus.WISH
                        onStatusChange(CollectionStatus.WISH)
                    }
                )
                StatusOption(
                    status = CollectionStatus.COLLECT,
                    label = stringResource(R.string.collection_collect),
                    isSelected = selectedStatus == CollectionStatus.COLLECT,
                    onSelect = {
                        selectedStatus = CollectionStatus.COLLECT
                        onStatusChange(CollectionStatus.COLLECT)
                    }
                )
                StatusOption(
                    status = CollectionStatus.ON_HOLD,
                    label = stringResource(R.string.collection_on_hold),
                    isSelected = selectedStatus == CollectionStatus.ON_HOLD,
                    onSelect = {
                        selectedStatus = CollectionStatus.ON_HOLD
                        onStatusChange(CollectionStatus.ON_HOLD)
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // 进度调整（如果有总集数）
            if (anime.totalEpisodes > 0) {
                Text(
                    text = stringResource(R.string.collection_progress_label),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (currentProgress > 0) {
                                currentProgress--
                                onProgressChange(currentProgress)
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_remove),
                            contentDescription = "Decrease"
                        )
                    }

                    Text(
                        text = "$currentProgress / ${anime.totalEpisodes}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    IconButton(
                        onClick = {
                            if (currentProgress < anime.totalEpisodes) {
                                currentProgress++
                                onProgressChange(currentProgress)
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_add),
                            contentDescription = "Increase"
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            }

            // 取消收藏按钮
            Button(
                onClick = { showRemoveDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(stringResource(R.string.collection_remove))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // 删除确认对话框
    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text(stringResource(R.string.collection_remove_confirm_title)) },
            text = { Text(stringResource(R.string.collection_remove_confirm_message, anime.displayName)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRemoveDialog = false
                        onRemove()
                        onDismiss()
                    }
                ) {
                    Text(
                        stringResource(R.string.confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun StatusOption(
    status: Int,
    label: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelect,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label)
    }
}
