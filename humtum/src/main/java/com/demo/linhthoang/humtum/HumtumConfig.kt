package com.demo.linhthoang.humtum

internal data class HumtumConfig(
    val ip: String,
    val websocket: String,
    val apiUrl: String = "/")