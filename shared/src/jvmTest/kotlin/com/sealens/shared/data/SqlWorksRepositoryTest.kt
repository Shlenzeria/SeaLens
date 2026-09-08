package com.sealens.shared.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.sealens.shared.db.SeaLensDb
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * 针对 SQLDelight 数据层的端到端测试：
 * 使用临时 SQLite 文件验证示例数据只 seed 一次、作品/章节 CRUD、
 * 正文保存，以及“关闭后重新打开同一数据库”后数据仍然存在。
 */
class SqlWorksRepositoryTest {

    @Test
    fun persistsWorksBoardsAndChapterContentAcrossReopen() = runBlocking {
        val dbFile = File.createTempFile("sealens-test-", ".db")
        dbFile.deleteOnExit()

        fun newRepository() = SqlWorksRepository(
            driverFactory = {
                JdbcSqliteDriver(
                    url = "jdbc:sqlite:${dbFile.absolutePath.replace('\\', '/')}",
                    properties = Properties(),
                    schema = SeaLensDb.Schema,
                )
            },
        )

        // 第一次“启动”：数据库被创建，示例数据 seed 一次。
        val first = newRepository()
        assertTrue(first.initialize())
        val seededIds = first.works.map { it.id }.toSet()
        assertTrue("sample-1" in seededIds, "首次启动应写入示例作品 sample-1")
        assertTrue("sample-2" in seededIds, "首次启动应写入示例作品 sample-2")

        // 新建作品：默认 6 个板块。
        val workId = assertNotNull(first.createWork("测试作品", "用于验证持久化"))
        val work = assertNotNull(first.workById(workId))
        assertEquals(6, work.sections.size)
        val bodyBoard = work.sections.first()
        assertEquals("正文", bodyBoard.title)

        // 新建章节并保存正文。
        val chapterId = assertNotNull(
            first.addChapter(workId, bodyBoard.id, "【01】开头"),
        )
        val content = "雾气漫过防波堤。\n\n第二段正文。"
        val saved = assertNotNull(
            first.saveChapterContent(workId, bodyBoard.id, chapterId, content),
        )
        assertEquals(content, saved.content)

        // 修改章节标题。
        assertTrue(first.renameChapter(workId, bodyBoard.id, chapterId, "【01】新标题"))

        // 第二次“启动”：同一数据库重新打开，不应再次 seed，数据应全部还在。
        val second = newRepository()
        assertTrue(second.initialize())
        val secondIds = second.works.map { it.id }.toSet()
        assertTrue(seededIds.all { it in secondIds }, "示例作品不应被重复 seed")
        assertTrue(workId in secondIds, "新建的作品应持久化")

        val reopenedWork = assertNotNull(second.workById(workId))
        assertEquals(6, reopenedWork.sections.size)
        val reopenedBoard = reopenedWork.sections.first { it.id == bodyBoard.id }
        val summary = reopenedBoard.chapters.first { it.id == chapterId }
        assertEquals("【01】新标题", summary.title)

        // 正文不在启动快照中，打开章节时才按需读取。
        val loaded = assertNotNull(
            second.loadChapterContent(workId, reopenedBoard.id, chapterId),
        )
        assertEquals(content, loaded.content)

        // 清空正文后保存也必须真正写库。
        val cleared = assertNotNull(
            second.saveChapterContent(workId, reopenedBoard.id, chapterId, ""),
        )
        assertEquals("", cleared.content)
        val afterClear = assertNotNull(
            second.loadChapterContent(workId, reopenedBoard.id, chapterId),
        )
        assertEquals("", afterClear.content)

        // 删除章节与删除作品后，重新打开同一数据库不应再看到它们。
        assertTrue(second.deleteChapter(workId, reopenedBoard.id, chapterId))
        val third = newRepository()
        assertTrue(third.initialize())
        assertEquals(null, third.chapterById(workId, reopenedBoard.id, chapterId))

        assertTrue(third.deleteWork(workId))
        val fourth = newRepository()
        assertTrue(fourth.initialize())
        assertEquals(null, fourth.workById(workId))
    }
}
