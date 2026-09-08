package com.sealens.shared.time

/** 当前系统时间（Unix 毫秒）。各平台分别提供实现。 */
expect fun currentTimeMillis(): Long
