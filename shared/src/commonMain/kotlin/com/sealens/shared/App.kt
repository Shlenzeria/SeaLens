package com.sealens.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sealens.shared.data.SqlWorksRepository
import com.sealens.shared.model.Chapter
import com.sealens.shared.model.Section
import com.sealens.shared.model.Work
import com.sealens.shared.theme.SeaLensColorMode
import com.sealens.shared.theme.SeaLensTheme
import com.sealens.shared.theme.SeaLensThemeChoice
import com.sealens.shared.ui.ChapterEditorScreen
import com.sealens.shared.ui.HomeScreen
import com.sealens.shared.ui.WorkspaceScreen
import com.sealens.shared.ui.ScaleButton
import kotlinx.coroutines.launch

/**
 * Shared entry point of the SeaLens UI.
 *
 * 页面路由只保存 id。应用启动时初始化 SQLite 并把
 * “作品 -> 板块 -> 章节摘要”读入仓库内存状态；
 * 章节正文在进入章节页时才单独读取。所有写库操作成功后才会更新界面状态。
 */
@Composable
fun App() {
    val repository = remember { SqlWorksRepository() }
    val scope = rememberCoroutineScope()
    var ready by remember { mutableStateOf(false) }
    var initFailed by remember { mutableStateOf(false) }
    var currentRoute by remember { mutableStateOf<Route>(Route.Home) }

    // Theme / color mode live at the top so every screen sees them.
    var themeChoice by remember { mutableStateOf(SeaLensThemeChoice.BlueGray) }
    var colorMode by remember { mutableStateOf(SeaLensColorMode.Light) }

    LaunchedEffect(Unit) {
        ready = repository.initialize()
        initFailed = !ready
        if (ready) {
            themeChoice = repository.loadTheme()
            colorMode = repository.loadColorMode()
        }
    }

    SeaLensTheme(themeChoice = themeChoice, colorMode = colorMode) {

        when {
            initFailed -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "数据库初始化失败",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(Modifier.height(12.dp))
                        ScaleButton(
                            onClick = {
                                scope.launch {
                                    ready = repository.initialize()
                                    initFailed = !ready
                                }
                            },
                        ) {
                            Text("重试")
                        }
                    }
                }
            }

            !ready -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "正在载入…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                val route = currentRoute
                when (route) {
                    Route.Home -> {
                        HomeScreen(
                            works = repository.works,
                            onOpenWork = { work -> currentRoute = Route.Workspace(work.id) },
                            onCreateWork = { title, description ->
                                scope.launch { repository.createWork(title, description) }
                            },
                            onUpdateWork = { id, title, description ->
                                scope.launch { repository.updateWork(id, title, description) }
                            },
                            onDeleteWork = { id ->
                                scope.launch { repository.deleteWork(id) }
                            },
                            themeChoice = themeChoice,
                            colorMode = colorMode,
                            onPickTheme = { picked ->
                                themeChoice = picked
                                scope.launch { repository.saveTheme(picked) }
                            },
                            onPickColorMode = { picked ->
                                colorMode = picked
                                scope.launch { repository.saveColorMode(picked) }
                            },
                        )
                    }

                    is Route.Workspace -> {
                        val work = repository.workById(route.workId)
                        val selectedSection = selectedSectionIn(work, route.sectionId)

                        if (work == null || selectedSection == null) {
                            LaunchedEffect(route.workId) {
                                currentRoute = Route.Home
                            }
                        } else {
                            WorkspaceScreen(
                                work = work,
                                selectedSection = selectedSection,
                                onSelectSection = { section ->
                                    currentRoute = route.copy(sectionId = section.id)
                                },
                                onBackToHome = { currentRoute = Route.Home },
                                onOpenChapter = { chapterId ->
                                    scope.launch {
                                        repository.loadChapterContent(
                                            workId = work.id,
                                            sectionId = selectedSection.id,
                                            chapterId = chapterId,
                                        )?.let { chapter ->
                                            currentRoute = Route.ChapterEditor(
                                                workId = work.id,
                                                sectionId = selectedSection.id,
                                                chapterId = chapterId,
                                                chapter = chapter,
                                            )
                                        }
                                    }
                                },
                                onCreateChapter = { sectionId, title ->
                                    scope.launch {
                                        val newChapterId =
                                            repository.addChapter(work.id, sectionId, title)
                                        if (newChapterId != null) {
                                            repository.loadChapterContent(
                                                workId = work.id,
                                                sectionId = sectionId,
                                                chapterId = newChapterId,
                                            )?.let { chapter ->
                                                currentRoute = Route.ChapterEditor(
                                                    workId = work.id,
                                                    sectionId = sectionId,
                                                    chapterId = newChapterId,
                                                    chapter = chapter,
                                                )
                                            }
                                        }
                                    }
                                },
                                onRenameChapter = { sectionId, chapterId, title ->
                                    scope.launch {
                                        repository.renameChapter(
                                            work.id,
                                            sectionId,
                                            chapterId,
                                            title,
                                        )
                                    }
                                },
                                onDeleteChapter = { sectionId, chapterId ->
                                    scope.launch {
                                        repository.deleteChapter(work.id, sectionId, chapterId)
                                    }
                                },
                            )
                        }
                    }

                    is Route.ChapterEditor -> {
                        ChapterEditorScreen(
                            chapter = route.chapter,
                            onSaveContent = { content ->
                                val saved = repository.saveChapterContent(
                                    workId = route.workId,
                                    sectionId = route.sectionId,
                                    chapterId = route.chapterId,
                                    content = content,
                                )
                                if (saved != null) {
                                    currentRoute = route.copy(chapter = saved)
                                }
                                saved != null
                            },
                            onBack = {
                                currentRoute = Route.Workspace(
                                    workId = route.workId,
                                    sectionId = route.sectionId,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun selectedSectionIn(work: Work?, sectionId: String?): Section? {
    if (work == null) return null
    return sectionId
        ?.let { id -> work.sections.firstOrNull { it.id == id } }
        ?: work.sections.firstOrNull()
}

private sealed interface Route {
    data object Home : Route

    data class Workspace(
        val workId: String,
        val sectionId: String? = null,
    ) : Route

    data class ChapterEditor(
        val workId: String,
        val sectionId: String,
        val chapterId: String,
        val chapter: Chapter,
    ) : Route
}
