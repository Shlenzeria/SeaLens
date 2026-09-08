package com.sealens.shared.model

/**
 * 一个独立作品（聚合根）。
 *
 * 数据关系：作品 -> 板块 -> 章节 -> 正文内容。
 * 每个作品携带自己的板块与章节，作品之间完全独立。
 */
data class Work(
    val id: String,
    val title: String,
    val description: String = "",
    val createdAt: Long,
    val modifiedAt: Long,
    val sections: List<Section> = emptyList(),
) {
    /** 全作品章节总数（跨全部板块合计）。 */
    val chapterCount: Int
        get() = sections.sumOf { it.chapters.size }
}
