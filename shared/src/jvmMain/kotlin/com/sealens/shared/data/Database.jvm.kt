package com.sealens.shared.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.sealens.shared.db.SeaLensDb
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.util.Properties

actual fun databaseDispatcher(): CoroutineDispatcher = Dispatchers.IO

actual fun createSeaLensDriver(): SqlDriver {
    val appData = System.getenv("APPDATA")
        ?.takeIf { it.isNotBlank() }
        ?: File(System.getProperty("user.home"), "AppData/Roaming").absolutePath
    val dataDir = File(appData, "SeaLens")
    if (!dataDir.isDirectory && !dataDir.mkdirs()) {
        error("无法创建 SeaLens 数据目录：$dataDir")
    }
    val dbFile = File(dataDir, "sealens.db")
    val jdbcPath = dbFile.absolutePath.replace('\\', '/')
    return JdbcSqliteDriver("jdbc:sqlite:$jdbcPath", Properties(), SeaLensDb.Schema)
}
