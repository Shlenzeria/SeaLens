package com.sealens.shared.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sealens.shared.model.Work
import com.sealens.shared.theme.SeaLensColorMode
import com.sealens.shared.theme.SeaLensTheme
import com.sealens.shared.theme.SeaLensThemeChoice

/**
 * 首页：“我的作品”列表。
 *
 * 支持：进入作品、新建作品、修改名称/简介、删除（删除前确认，删除时同步本地数据库）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    works: List<Work>,
    onOpenWork: (Work) -> Unit,
    onCreateWork: (title: String, description: String) -> Unit,
    onUpdateWork: (id: String, title: String, description: String) -> Unit,
    onDeleteWork: (id: String) -> Unit,
    themeChoice: SeaLensThemeChoice,
    colorMode: SeaLensColorMode,
    onPickTheme: (SeaLensThemeChoice) -> Unit,
    onPickColorMode: (SeaLensColorMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        ThemeSettingsDialog(
            currentTheme = themeChoice,
            currentMode = colorMode,
            onPickTheme = onPickTheme,
            onPickMode = onPickColorMode,
            onDismiss = { showSettings = false },
        )
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<Work?>(null) }
    var deleteTarget by remember { mutableStateOf<Work?>(null) }

    if (showCreateDialog) {
        WorkFormDialog(
            dialogTitle = "新建作品",
            confirmText = "创建",
            initialTitle = "",
            initialDescription = "",
            onConfirm = { title, description ->
                onCreateWork(title, description)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false },
        )
    }

    editTarget?.let { target ->
        WorkFormDialog(
            dialogTitle = "修改作品",
            confirmText = "保存",
            initialTitle = target.title,
            initialDescription = target.description,
            onConfirm = { title, description ->
                onUpdateWork(target.id, title, description)
                editTarget = null
            },
            onDismiss = { editTarget = null },
        )
    }

    deleteTarget?.let { target ->
        ConfirmDialog(
            title = "删除作品",
            message = "确定删除「${target.title}」吗？\n\n该作品及其全部板块、章节会从本地数据库永久删除，此操作无法撤销。",
            onConfirm = {
                onDeleteWork(target.id)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SeaLensTopBar(
                title = "SeaLens",
                actions = {
                    ScaleTextButton(
                        text = "搜索",
                        onClick = { /* TODO: 搜索，后续版本接入 */ },
                    )
                    ScaleTextButton(
                        text = "设置",
                        onClick = { showSettings = true },
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Text(
                text = "我的作品",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 28.dp, bottom = 18.dp),
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 250.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
            ) {
                items(works, key = { it.id }) { work ->
                    WorkCard(
                        work = work,
                        onOpen = { onOpenWork(work) },
                        onEdit = { editTarget = work },
                        onDelete = { deleteTarget = work },
                    )
                }
                item(key = "new-work") {
                    NewWorkCard(onClick = { showCreateDialog = true })
                }
            }
        }
    }
}

@Composable
private fun WorkCard(
    work: Work,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactive = rememberInteractiveSurface(
        restColor = MaterialTheme.colorScheme.surface,
        hoverColor = SeaLensTheme.extras.hover,
        pressedColor = SeaLensTheme.extras.pressed,
    )
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(184.dp)
            .then(interactive.hoverModifier)
            .clickable(
                onClick = onOpen,
                interactionSource = interactive.interactionSource,
                indication = null,
            )
            .hoverScale(interactive.interactionSource),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = interactive.surfaceColor,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = work.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = work.description.ifBlank { "暂无简介" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${work.chapterCount} 章",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.weight(1f))
                ScaleTextButton(
                    text = "编辑",
                    onClick = onEdit,
                )
                ScaleTextButton(
                    text = "删除",
                    onClick = onDelete,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun NewWorkCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactive = rememberInteractiveSurface(
        restColor = MaterialTheme.colorScheme.surfaceVariant,
        hoverColor = SeaLensTheme.extras.hover,
        pressedColor = SeaLensTheme.extras.pressed,
    )
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(184.dp)
            .then(interactive.hoverModifier)
            .clickable(
                onClick = onClick,
                interactionSource = interactive.interactionSource,
                indication = null,
            )
            .hoverScale(interactive.interactionSource),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = interactive.surfaceColor,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "＋",
                fontSize = 30.sp,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "新建作品",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
