package com.sealens.shared.model

/**
 * 作品内的板块（正文、番外、设定集等）。
 *
 * 板块只从属于某一个作品，不允许跨作品共享。
 */
data class Section(
    val id: String,
    val title: String,
    val chapters: List<Chapter> = emptyList(),
)
