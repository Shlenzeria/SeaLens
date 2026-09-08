package com.sealens.shared.model

/**
 * 章节正文。
 *
 * [sortOrder] 表示章节在所属板块内的排序位置；章节名称本身不强制使用
 * 数字编号，允许“【09】风波(1)”这类普通字符串。
 */
data class Chapter(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val modifiedAt: Long,
    val sortOrder: Int,
)
