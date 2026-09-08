package com.sealens.app.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.sealens.shared.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SeaLens",
        state = rememberWindowState(placement = WindowPlacement.Maximized),
    ) {
        App()
    }
}
