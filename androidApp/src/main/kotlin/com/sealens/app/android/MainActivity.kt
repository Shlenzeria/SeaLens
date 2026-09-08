package com.sealens.app.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sealens.shared.App
import com.sealens.shared.data.initSeaLensDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        initSeaLensDatabase(applicationContext)
        setContent {
            App()
        }
    }
}
