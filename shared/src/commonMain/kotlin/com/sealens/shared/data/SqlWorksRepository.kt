package com.sealens.shared.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cash.sqldelight.db.SqlDriver
import com.sealens.shared.db.SeaLensDb
import com.sealens.shared.model.Chapter
import com.sealens.shared.model.Section
import com.sealens.shared.model.Work
import com.sealens.shared.time.currentTimeMillis
import com.sealens.shared.theme.SeaLensColorMode
import com.sealens.shared.theme.SeaLensThemeChoice
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * 基于 SQLite（SQLDelight）的作品仓库。
 *
 * - 启动时把「作品 -> 板块 -> 章节摘要」读入内存供 Compose 直接展示；
 *   章节正文只在该章节被打开时才按 id 读取，不提前加载全文。
 * - 所有数据库读写都在后台调度器上执行，并通过互斥锁串行化，避免 JDBC
 *   单连接在不同线程上并发访问；写库成功后才更新 Compose 状态。
 * - 本阶段没有自动保存，也没有回收站/版本历史等后续功能。
 */
class SqlWorksRepository(
    private val driverFactory: () -> SqlDriver = { createSeaLensDriver() },
) {

    /** 当前作品列表（含板块与章节摘要，章节 content 为空字符串）。 */
    var works by mutableStateOf<List<Work>>(emptyList())
        private set

    private val dispatcher: CoroutineDispatcher = databaseDispatcher()
    private val dbMutex = Mutex()
    private var database: SeaLensDb? = null
    private var idSequence = 0L

    /**
     * 打开数据库、写入首次启动的示例数据，然后读取当前作品快照。
     * 可以重复调用；已打开的数据库不会重复创建。
     */
    suspend fun initialize(): Boolean {
        val snapshot = runCatching {
            dbCall {
                val db = database ?: SeaLensDb(driverFactory()).also { opened ->
                    database = opened
                }
                seedSampleDataIfNeeded(db)
                loadWorksSnapshot(db)
            }
        }.getOrNull() ?: return false
        works = snapshot
        return true
    }

    fun workById(id: String): Work? = works.firstOrNull { it.id == id }

    fun sectionById(workId: String, sectionId: String): Section? =
        workById(workId)?.sections?.firstOrNull { it.id == sectionId }

    fun chapterById(workId: String, sectionId: String, chapterId: String): Chapter? =
        sectionById(workId, sectionId)?.chapters?.firstOrNull { it.id == chapterId }

    /** 新建作品并写入数据库，同时创建默认的 6 个板块。 */
    suspend fun createWork(title: String, description: String): String? {
        if (title.isBlank()) return null
        var newWorkId: String? = null
        val snapshot = runCatching {
            dbCall {
                val db = openedDatabase()
                val id = nextIdLocked("work")
                newWorkId = id
                val now = currentTimeMillis()
                val work = Work(
                    id = id,
                    title = title.trim(),
                    description = description.trim(),
                    createdAt = now,
                    modifiedAt = now,
                    sections = defaultSectionsFor(id),
                )
                insertWorkTree(db, work)
                loadWorksSnapshot(db)
            }
        }.getOrNull() ?: return null
        works = snapshot
        return newWorkId
    }

    /** 修改作品名称/简介，写库成功后同步内存状态。 */
    suspend fun updateWork(id: String, title: String, description: String): Boolean {
        val existing = workById(id) ?: return false
        if (title.isBlank()) return false
        if (existing.title == title.trim() && existing.description == description.trim()) return true

        val snapshot = runCatching {
            dbCall {
                val db = openedDatabase()
                db.seaLensDbQueries.updateWork(
                    title.trim(),
                    description.trim(),
                    currentTimeMillis(),
                    id,
                )
                loadWorksSnapshot(db)
            }
        }.getOrNull() ?: return false
        works = snapshot
        return true
    }

    /**
     * 删除作品及其全部板块、章节（本地数据库中的真实删除；
     * 本阶段不提供回收站）。
     */
    suspend fun deleteWork(id: String): Boolean {
        if (workById(id) == null) return false
        val snapshot = runCatching {
            dbCall {
                val db = openedDatabase()
                val queries = db.seaLensDbQueries
                val boards = queries.selectBoardsByWork(id).executeAsList()
                boards.forEach { board ->
                    queries.deleteChaptersByBoard(board.id)
                }
                queries.deleteBoardsByWork(id)
                queries.deleteWork(id)
                loadWorksSnapshot(db)
            }
        }.getOrNull() ?: return false
        works = snapshot
        return true
    }

    /** 在某板块下新建章节，写库成功返回新章节 id。 */
    suspend fun addChapter(workId: String, sectionId: String, title: String): String? {
        if (title.isBlank()) return null
        if (sectionById(workId, sectionId) == null) return null

        var newChapterId: String? = null
        val snapshot = runCatching {
            dbCall {
                val db = openedDatabase()
                val queries = db.seaLensDbQueries
                val maxOrder = queries.selectChapterSummariesByBoard(sectionId)
                    .executeAsList()
                    .maxOfOrNull { it.orderIndex }
                    ?: 0L
                val now = currentTimeMillis()
                val id = nextIdLocked("chapter")
                newChapterId = id
                queries.insertChapter(id, sectionId, title.trim(), "", maxOrder + 1L, now, now)
                touchWork(db, workId, now)
                loadWorksSnapshot(db)
            }
        }.getOrNull() ?: return null
        works = snapshot
        return newChapterId
    }

    /** 重命名章节，写库成功后同步内存状态。 */
    suspend fun renameChapter(
        workId: String,
        sectionId: String,
        chapterId: String,
        title: String,
    ): Boolean {
        val chapter = chapterById(workId, sectionId, chapterId) ?: return false
        if (title.isBlank()) return false
        if (chapter.title == title.trim()) return true

        val snapshot = runCatching {
            dbCall {
                val db = openedDatabase()
                val now = currentTimeMillis()
                db.seaLensDbQueries.updateChapterTitle(title.trim(), now, chapterId)
                touchWork(db, workId, now)
                loadWorksSnapshot(db)
            }
        }.getOrNull() ?: return false
        works = snapshot
        return true
    }

    /** 删除单个章节（真实删除，本阶段无回收站）。 */
    suspend fun deleteChapter(workId: String, sectionId: String, chapterId: String): Boolean {
        if (chapterById(workId, sectionId, chapterId) == null) return false
        val snapshot = runCatching {
            dbCall {
                val db = openedDatabase()
                val now = currentTimeMillis()
                db.seaLensDbQueries.deleteChapter(chapterId)
                touchWork(db, workId, now)
                loadWorksSnapshot(db)
            }
        }.getOrNull() ?: return false
        works = snapshot
        return true
    }

    /**
     * 打开章节时才从数据库读取完整正文。
     * [workId]/[sectionId] 用于保持调用语义一致，实际按全局唯一 chapterId 查询。
     */
    suspend fun loadChapterContent(workId: String, sectionId: String, chapterId: String): Chapter? =
        runCatching {
            dbCall {
                val queries = openedDatabase().seaLensDbQueries
                val row = queries.selectChapterById(chapterId).executeAsOneOrNull()
                    ?: return@dbCall null
                Chapter(
                    id = row.id,
                    title = row.title,
                    content = row.content,
                    createdAt = row.createdAt,
                    modifiedAt = row.modifiedAt,
                    sortOrder = row.orderIndex.toInt(),
                )
            }
        }.getOrNull()

    /**
     * 手动保存章节正文。只有数据库确认写入成功才返回更新后的章节；
     * 失败返回 null，调用方应保留编辑器草稿并允许重试。
     */
    suspend fun saveChapterContent(
        workId: String,
        sectionId: String,
        chapterId: String,
        content: String,
    ): Chapter? {
        // 内存中的章节摘要是没有 content 的，因此这里不比较摘要，
        // 一律真实写库，确保“清空正文后保存”也能持久化。
        if (chapterById(workId, sectionId, chapterId) == null) return null

        val updatedRow = runCatching {
            dbCall {
                val queries = openedDatabase().seaLensDbQueries
                val now = currentTimeMillis()
                queries.updateChapterContent(content, now, chapterId)
                val after = queries.selectChapterById(chapterId).executeAsOne()
                if (after.content == content && after.modifiedAt == now) after else null
            }
        }.getOrNull() ?: return null

        val savedChapter = Chapter(
            id = updatedRow.id,
            title = updatedRow.title,
            content = updatedRow.content,
            createdAt = updatedRow.createdAt,
            modifiedAt = updatedRow.modifiedAt,
            sortOrder = updatedRow.orderIndex.toInt(),
        )
        works = works.map { work ->
            if (work.id != workId) {
                work
            } else {
                work.copy(
                    sections = work.sections.map { section ->
                        if (section.id != sectionId) {
                            section
                        } else {
                            section.copy(
                                chapters = section.chapters.map { chapter ->
                                    if (chapter.id == chapterId) savedChapter else chapter
                                },
                            )
                        }
                    },
                )
            }
        }
        return savedChapter
    }


    /** Read the saved theme choice; defaults to BlueGray. */
    suspend fun loadTheme(): SeaLensThemeChoice = dbCall {
        val raw = openedDatabase().seaLensDbQueries
            .selectMetaValue("theme_choice").executeAsOneOrNull()
        runCatching { SeaLensThemeChoice.valueOf(raw ?: "BlueGray") }
            .getOrDefault(SeaLensThemeChoice.BlueGray)
    }

    /** Read the saved color mode; defaults to Light. */
    suspend fun loadColorMode(): SeaLensColorMode = dbCall {
        val raw = openedDatabase().seaLensDbQueries
            .selectMetaValue("color_mode").executeAsOneOrNull()
        runCatching { SeaLensColorMode.valueOf(raw ?: "Light") }
            .getOrDefault(SeaLensColorMode.Light)
    }

    /** Persist the chosen theme choice; called after the user changes it in Settings. */
    suspend fun saveTheme(choice: SeaLensThemeChoice) {
        dbCall {
            openedDatabase().seaLensDbQueries.insertMeta("theme_choice", choice.name)
        }
    }

    /** Persist the chosen color mode; called after the user changes it in Settings. */
    suspend fun saveColorMode(mode: SeaLensColorMode) {
        dbCall {
            openedDatabase().seaLensDbQueries.insertMeta("color_mode", mode.name)
        }
    }

    /** 在后台线程串行执行数据库访问。 */
    private suspend fun <T> dbCall(block: () -> T): T =
        withContext(dispatcher) {
            dbMutex.withLock {
                block()
            }
        }

    private fun openedDatabase(): SeaLensDb =
        database ?: error("SeaLens 数据库尚未初始化")

    /** 只有在数据库首次创建（app_meta 中无 seeded 标记）时才写入示例数据。 */
    private fun seedSampleDataIfNeeded(db: SeaLensDb) {
        val queries = db.seaLensDbQueries
        val seeded = queries.selectMetaValue("seeded").executeAsOneOrNull()
        if (seeded != null) return

        db.transaction {
            createSampleWorks().forEach { work ->
                insertWorkTree(db, work)
            }
            queries.insertMeta("seeded", "1")
        }
    }

    /** 作品、其全部板块与章节写入数据库；板块/章节顺序按列表位置 1 起计。 */
    private fun insertWorkTree(db: SeaLensDb, work: Work) {
        val queries = db.seaLensDbQueries
        queries.insertWork(work.id, work.title, work.description, work.createdAt, work.modifiedAt)
        work.sections.forEachIndexed { boardIndex, section ->
            queries.insertBoard(section.id, work.id, section.title, (boardIndex + 1).toLong())
            section.chapters.forEachIndexed { chapterIndex, chapter ->
                queries.insertChapter(
                    chapter.id,
                    section.id,
                    chapter.title,
                    chapter.content,
                    (chapterIndex + 1).toLong(),
                    chapter.createdAt,
                    chapter.modifiedAt,
                )
            }
        }
    }

    /** 同步刷新作品的 modifiedAt（增删改章节时调用）。 */
    private fun touchWork(db: SeaLensDb, workId: String, now: Long) {
        val queries = db.seaLensDbQueries
        val row = queries.selectWorkById(workId).executeAsOneOrNull() ?: return
        queries.updateWork(row.title, row.description, now, workId)
    }

    /**
     * 从数据库读取「作品 + 板块 + 章节摘要」。
     * 这里刻意不读取 content，章节全文由 loadChapterContent 按需加载。
     */
    private fun loadWorksSnapshot(db: SeaLensDb): List<Work> {
        val queries = db.seaLensDbQueries
        val workRows = queries.selectAllWorks().executeAsList()
        return workRows.map { workRow ->
            val sections = queries.selectBoardsByWork(workRow.id).executeAsList().map { boardRow ->
                val chapters = queries.selectChapterSummariesByBoard(boardRow.id)
                    .executeAsList()
                    .map { chapterRow ->
                        Chapter(
                            id = chapterRow.id,
                            title = chapterRow.title,
                            content = "",
                            createdAt = chapterRow.createdAt,
                            modifiedAt = chapterRow.modifiedAt,
                            sortOrder = chapterRow.orderIndex.toInt(),
                        )
                    }
                Section(
                    id = boardRow.id,
                    title = boardRow.name,
                    chapters = chapters,
                )
            }
            Work(
                id = workRow.id,
                title = workRow.title,
                description = workRow.description,
                createdAt = workRow.createdAt,
                modifiedAt = workRow.modifiedAt,
                sections = sections,
            )
        }
    }

    /** 生成跨平台、跨重启都不容易冲突的字符串 id（在互斥锁内调用）。 */
    private fun nextIdLocked(prefix: String): String {
        idSequence += 1
        return "$prefix-${currentTimeMillis()}-$idSequence"
    }
}
