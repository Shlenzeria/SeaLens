package com.sealens.shared.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sealens.shared.model.Chapter
import com.sealens.shared.theme.SeaLensTheme
import kotlinx.coroutines.launch

/**
 * 章节查看/编辑页。
 *
 * 保存按钮会真正写入本地 SQLite：数据库确认写入成功后才显示“已保存”；
 * 失败时保留编辑区草稿并允许重试。本页面不做自动保存。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterEditorScreen(
    chapter: Chapter,
    onSaveContent: suspend (content: String) -> Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember(chapter.id) { mutableStateOf(chapter.content) }
    var saveState by remember(chapter.id) { mutableStateOf(SaveState.Idle) }
    val scope = rememberCoroutineScope()
    val isDirty = draft != chapter.content
    val charCount = draft.count { !it.isWhitespace() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SeaLensTopBar(
                title = chapter.title,
                onBack = onBack,
                backLabel = "章节列表",
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp, vertical = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$charCount 字",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.weight(1f))
                if (saveState == SaveState.Saved && !isDirty) {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .background(
                                color = SeaLensTheme.extras.selected,
                                shape = RoundedCornerShape(8.dp),
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "已保存到本地",
                            style = MaterialTheme.typography.labelLarge,
                            color = SeaLensTheme.extras.selectedText,
                        )
                    }
                }
                if (saveState == SaveState.Failed) {
                    Text(
                        text = "保存失败，内容未丢失",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(end = 16.dp),
                    )
                }
                ScaleButton(
                    onClick = {
                        scope.launch {
                            saveState = SaveState.Saving
                            val saved = onSaveContent(draft)
                            saveState = if (saved) SaveState.Saved else SaveState.Failed
                        }
                    },
                    enabled = isDirty && saveState != SaveState.Saving,
                ) {
                    Text(if (saveState == SaveState.Saving) "保存中…" else "保存")
                }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .border(
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                BasicTextField(
                    value = draft,
                    onValueChange = {
                        draft = it
                        if (saveState == SaveState.Saved) {
                            saveState = SaveState.Idle
                        }
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 28.sp,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private enum class SaveState {
    Idle,
    Saving,
    Saved,
    Failed,
}
