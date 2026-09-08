package com.sealens.shared.data

import com.sealens.shared.model.Chapter
import com.sealens.shared.model.Section
import com.sealens.shared.model.Work
import com.sealens.shared.time.currentTimeMillis

/**
 * 数据库首次创建时写入一次的示例数据。
 *
 * 之后用户对这些作品的修改会像普通作品一样持久化；删除后不会再次出现。
 */
fun createSampleWorks(): List<Work> {
    val now = currentTimeMillis()
    val day = 24L * 60L * 60L * 1000L

    val lighthouse = Work(
        id = "sample-1",
        title = "雾海灯塔",
        description = "旧灯塔管理员留下了一本没有署名的航海日志。",
        createdAt = now - 32 * day,
        modifiedAt = now - 2 * day,
        sections = sectionsWithChapters(
            workId = "sample-1",
            now = now,
            day = day,
            chaptersByTitle = mapOf(
                "正文" to listOf(
                    Triple("【01】雾中的光", "雾气漫过防波堤的时候，灯塔刚好亮起第一束光。\n\n守塔人没有按时交接，这已经是第三天。", 28L * day),
                    Triple("【02】值班表", "值班表上用红笔圈出了七月十四日，旁边只有两个小字：换人。", 20L * day),
                ),
                "设定集" to listOf(
                    Triple("灯塔与航道", "灯塔建于民国十七年，灯质为闪白 5 秒，射程约 18 海里。", 12L * day),
                ),
            ),
        ),
    )

    val southernLetters = Work(
        id = "sample-2",
        title = "南方的来信",
        description = "关于小镇、雨季和许多年未寄出的信。",
        createdAt = now - 18 * day,
        modifiedAt = now - 1 * day,
        sections = sectionsWithChapters(
            workId = "sample-2",
            now = now,
            day = day,
            chaptersByTitle = mapOf(
                "正文" to listOf(
                    Triple("【01】雨季", "雨从四月开始就没停过。邮筒锈成了深褐色，钥匙插进去要费一点力气。", 16L * day),
                    Triple("【02】未寄出的信", "信封上没有收件人，邮票倒是贴得整整齐齐。", 8L * day),
                ),
                "番外" to listOf(
                    Triple("车站的一夜", "末班车过去以后，站台只剩一盏白炽灯和等不到的人。", 3L * day),
                ),
            ),
        ),
    )

    return listOf(lighthouse, southernLetters)
}

private fun sectionsWithChapters(
    workId: String,
    now: Long,
    day: Long,
    chaptersByTitle: Map<String, List<Triple<String, String, Long>>>,
): List<Section> {
    var chapterSequence = 0
    return defaultSectionsFor(workId).map { section ->
        val templates = chaptersByTitle[section.title].orEmpty()
        section.copy(
            chapters = templates.mapIndexed { index, (title, content, age) ->
                chapterSequence += 1
                Chapter(
                    id = "$workId-chapter-$chapterSequence",
                    title = title,
                    content = content,
                    createdAt = now - age,
                    modifiedAt = now - (age / 2),
                    sortOrder = index + 1,
                )
            },
        )
    }
}
