package com.sealens.shared.data

import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.CoroutineDispatcher

/** 创建平台对应的 SQLite 驱动；数据库文件位于应用自己的本地数据目录。 */
expect fun createSeaLensDriver(): SqlDriver

/** 数据库访问使用的后台调度器。 */
expect fun databaseDispatcher(): CoroutineDispatcher
