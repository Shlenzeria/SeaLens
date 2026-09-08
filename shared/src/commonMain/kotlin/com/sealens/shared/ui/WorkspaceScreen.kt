package com.sealens.shared.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sealens.shared.model.Chapter
import com.sealens.shared.model.Section
import com.sealens.shared.model.Work
import com.sealens.shared.theme.SeaLensTheme

/**
 * 作品工作区：左侧板块导航 + 右侧当前板块的章节列表。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    work: Work,
    selectedSection: Section,
    onSelectSection: (Section) -> Unit,
    onBackToHome: () -> Unit,
    onOpenChapter: (chapterId: String) -> Unit,
    onCreateChapter: (sectionId: String, title: String) -> Unit,
    onRenameChapter: (sectionId: String, chapterId: String, title: String) -> Unit,
    onDeleteChapter: (sectionId: String, chapterId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCreateChapter by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<Chapter?>(null) }

    if (showCreateChapter) {
        ChapterTitleDialog(
            dialogTitle = "新建章节",
            confirmText = "创建",
            onConfirm = { title ->
                onCreateChapter(selectedSection.id, title)
                showCreateChapter = false
            },
            onDismiss = { showCreateChapter = false },
        )
    }

    renameTarget?.let { chapter ->
        ChapterTitleDialog(
            dialogTitle = "重命名章节",
            confirmText = "保存",
            initialTitle = chapter.title,
            onConfirm = { title ->
                onRenameChapter(selectedSection.id, chapter.id, title)
                renameTarget = null
            },
            onDismiss = { renameTarget = null },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SeaLensTopBar(
                title = work.title,
                onBack = onBackToHome,
                backLabel = "我的作品",
            )
        },
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            SectionNavigation(
                sections = work.sections,
                selectedSectionId = selectedSection.id,
                onSelectSection = onSelectSection,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, end = 32.dp, top = 26.dp, bottom = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedSection.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = "${selectedSection.chapters.size} 章",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    ScaleButton(onClick = { showCreateChapter = true }) {
                        Text("＋ 新建章节")
                    }
                }

                if (selectedSection.chapters.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "这个板块还没有章节",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 32.dp),
                    ) {
                        items(selectedSection.chapters, key = { it.id }) { chapter ->
                            ChapterRow(
                                chapter = chapter,
                                onOpen = { onOpenChapter(chapter.id) },
                                onRename = { renameTarget = chapter },
                                onDelete = { onDeleteChapter(selectedSection.id, chapter.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionNavigation(
    sections: List<Section>,
    selectedSectionId: String,
    onSelectSection: (Section) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(200.dp)
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Text(
            text = "板块",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 10.dp),
        )
        sections.forEach { section ->
            val selected = section.id == selectedSectionId
            val interactive = rememberInteractiveSurface(
                restColor = if (selected) {
                    SeaLensTheme.extras.selected
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                hoverColor = if (selected) {
                    SeaLensTheme.extras.selected
                } else {
                    SeaLensTheme.extras.hover
                },
                pressedColor = SeaLensTheme.extras.pressed,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(interactive.hoverModifier)
                    .background(color = interactive.surfaceColor)
                    .clickable(
                        onClick = { onSelectSection(section) },
                        interactionSource = interactive.interactionSource,
                        indication = null,
                    )
                    .padding(start = 16.dp, top = 13.dp, end = 16.dp, bottom = 13.dp)
                    .hoverScale(interactive.interactionSource),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(18.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(2.dp),
                            ),
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selected) {
                        SeaLensTheme.extras.selectedText
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ChapterRow(
    chapter: Chapter,
    onOpen: () -> Unit,
    onRename: () -> Unit,
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
            .then(interactive.hoverModifier)
            .clickable(
                onClick = onOpen,
                interactionSource = interactive.interactionSource,
                indication = null,
            )
            .hoverScale(interactive.interactionSource),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = interactive.surfaceColor,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = chapter.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            ScaleTextButton(
                text = "重命名",
                onClick = onRename,
            )
            ScaleTextButton(
                text = "删除",
                onClick = onDelete,
                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
