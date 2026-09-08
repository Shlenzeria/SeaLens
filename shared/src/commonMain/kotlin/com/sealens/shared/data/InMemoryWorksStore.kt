package com.sealens.shared.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sealens.shared.model.Chapter
import com.sealens.shared.model.Section
import com.sealens.shared.model.Work
import com.sealens.shared.time.currentTimeMillis

/**
 * 仅存在于内存中的作品仓库。
 *
 * 所有操作都直接修改内存状态，不做任何持久化；以后接入 SQLite 时，
 * 用数据源实现替换本类即可，UI 层不需要改变调用方式。
 */
class InMemoryWorksStore {

    var works by mutableStateOf(createSampleWorks())
        private set

    private var sequence = 0L

    fun workById(id: String): Work? = works.firstOrNull { it.id == id }

    fun chapterById(workId: String, sectionId: String, chapterId: String): Chapter? {
        val section = sectionById(workId, sectionId) ?: return null
        return section.chapters.firstOrNull { it.id == chapterId }
    }

    fun sectionById(workId: String, sectionId: String): Section? =
        workById(workId)?.sections?.firstOrNull { it.id == sectionId }

    fun createWork(title: String, description: String): String {
        val now = currentTimeMillis()
        val id = nextId("work")
        val work = Work(
            id = id,
            title = title.trim(),
            description = description.trim(),
            createdAt = now,
            modifiedAt = now,
            sections = defaultSectionsFor(id),
        )
        works = works + work
        return id
    }

    fun updateWork(id: String, title: String, description: String): Boolean {
        val work = workById(id) ?: return false
        if (title.isBlank()) return false

        val now = currentTimeMillis()
        works = works.map {
            if (it.id == id) {
                it.copy(
                    title = title.trim(),
                    description = description.trim(),
                    modifiedAt = now,
                )
            } else {
                it
            }
        }
        return work.title != title.trim() || work.description != description.trim()
    }

    fun deleteWork(id: String): Boolean {
        val existed = workById(id) != null
        if (existed) {
            works = works.filterNot { it.id == id }
        }
        return existed
    }

    fun addChapter(workId: String, sectionId: String, title: String): String? {
        if (title.isBlank()) return null
        val work = workById(workId) ?: return null
        val section = work.sections.firstOrNull { it.id == sectionId } ?: return null

        val now = currentTimeMillis()
        val nextOrder = (section.chapters.maxOfOrNull { it.sortOrder } ?: 0) + 1
        val chapter = Chapter(
            id = nextId("chapter"),
            title = title.trim(),
            content = "",
            createdAt = now,
            modifiedAt = now,
            sortOrder = nextOrder,
        )

        works = works.map { current ->
            if (current.id == workId) {
                current.copy(
                    modifiedAt = now,
                    sections = current.sections.map { item ->
                        if (item.id == sectionId) {
                            item.copy(chapters = item.chapters + chapter)
                        } else {
                            item
                        }
                    },
                )
            } else {
                current
            }
        }
        return chapter.id
    }

    fun renameChapter(workId: String, sectionId: String, chapterId: String, title: String): Boolean {
        if (title.isBlank()) return false
        val chapter = chapterById(workId, sectionId, chapterId) ?: return false
        if (chapter.title == title.trim()) return false

        val now = currentTimeMillis()
        works = works.map { work ->
            if (work.id != workId) {
                work
            } else {
                work.copy(
                    modifiedAt = now,
                    sections = work.sections.map { section ->
                        if (section.id != sectionId) {
                            section
                        } else {
                            section.copy(
                                chapters = section.chapters.map { item ->
                                    if (item.id == chapterId) {
                                        item.copy(title = title.trim(), modifiedAt = now)
                                    } else {
                                        item
                                    }
                                },
                            )
                        }
                    },
                )
            }
        }
        return true
    }

    fun deleteChapter(workId: String, sectionId: String, chapterId: String): Boolean {
        val work = workById(workId) ?: return false
        val section = work.sections.firstOrNull { it.id == sectionId } ?: return false
        if (section.chapters.none { it.id == chapterId }) return false

        val now = currentTimeMillis()
        works = works.map { current ->
            if (current.id != workId) {
                current
            } else {
                current.copy(
                    modifiedAt = now,
                    sections = current.sections.map { item ->
                        if (item.id == sectionId) {
                            item.copy(chapters = item.chapters.filterNot { it.id == chapterId })
                        } else {
                            item
                        }
                    },
                )
            }
        }
        return true
    }

    fun saveChapterContent(workId: String, sectionId: String, chapterId: String, content: String): Boolean {
        val chapter = chapterById(workId, sectionId, chapterId) ?: return false
        if (chapter.content == content) return false

        val now = currentTimeMillis()
        works = works.map { work ->
            if (work.id != workId) {
                work
            } else {
                work.copy(
                    modifiedAt = now,
                    sections = work.sections.map { section ->
                        if (section.id != sectionId) {
                            section
                        } else {
                            section.copy(
                                chapters = section.chapters.map { item ->
                                    if (item.id == chapterId) {
                                        item.copy(content = content, modifiedAt = now)
                                    } else {
                                        item
                                    }
                                },
                            )
                        }
                    },
                )
            }
        }
        return true
    }

    private fun nextId(prefix: String): String = "$prefix-${++sequence}"
}
