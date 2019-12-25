package com.demo.linhthoang.humtum

import com.auth0.android.result.Credentials

open class HumtumCredential(credentials: Credentials) {
    val idToken = credentials.idToken
}