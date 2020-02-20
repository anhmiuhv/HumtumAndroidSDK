package com.demo.linhthoang.humtum

internal data class HumtumConfig(
    val ip: String,
    val websocket: String,
    val apiUrl: String = "/",
    val clientid: String = "123",
    val clientSecret: String = "456")