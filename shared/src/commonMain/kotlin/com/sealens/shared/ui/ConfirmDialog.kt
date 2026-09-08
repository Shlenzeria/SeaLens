package com.sealens.shared.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/** 通用删除/危险操作确认对话框。 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "删除",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(18.dp),
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            ScaleTextButton(
                text = confirmText,
                onClick = onConfirm,
            )
        },
        dismissButton = {
            ScaleTextButton(
                text = "取消",
                onClick = onDismiss,
            )
        },
    )
}
