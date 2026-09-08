package com.sealens.shared.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.sealens.shared.db.SeaLensDb
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

private var appContext: Context? = null

/**
 * 在 Activity 创建时、setContent 之前调用一次，用于取得应用的 Context。
 * Android 的数据库文件存放在应用私有数据目录中（无需也不应暴露到外部）。
 */
fun initSeaLensDatabase(context: Context) {
    appContext = context.applicationContext
}

actual fun databaseDispatcher(): CoroutineDispatcher = Dispatchers.IO

actual fun createSeaLensDriver(): SqlDriver {
    val context = checkNotNull(appContext) {
        "initSeaLensDatabase() 必须在创建数据库前调用"
    }
    return AndroidSqliteDriver(SeaLensDb.Schema, context, "sealens.db")
}
