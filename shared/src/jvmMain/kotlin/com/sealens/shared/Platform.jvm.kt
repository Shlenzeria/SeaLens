package com.sealens.shared

actual fun platformName(): String =
    "Windows / JVM ${System.getProperty("java.version")}"
