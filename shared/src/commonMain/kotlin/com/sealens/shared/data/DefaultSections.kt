package com.sealens.shared.data

import com.sealens.shared.model.Section

/** 每个作品默认拥有的板块名称。 */
val DefaultSectionTitles: List<String> = listOf(
    "正文",
    "番外",
    "设定集",
    "正文修改记录",
    "杂谈",
    "废稿",
)

/** 为某个作品创建一套空的默认板块。 */
fun defaultSectionsFor(workId: String): List<Section> =
    DefaultSectionTitles.mapIndexed { index, title ->
        Section(
            id = "$workId-section-${index + 1}",
            title = title,
        )
    }
