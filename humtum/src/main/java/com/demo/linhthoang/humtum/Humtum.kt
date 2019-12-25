package com.demo.linhthoang.humtum

import android.util.Log
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

val TAG = "Humtum"
open class Humtum(val config: HumtumConfig, val credentials: HumtumCredential) {
    private val baseUrl = "${config.ip}${config.apiUrl}"
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        })
        .build()


    fun getSelf(_onSuccess: (json: JSONObject) -> Unit, _onFailure: (e: java.lang.Exception) -> Unit = { e -> throw e }) {
        val url = "${baseUrl}users/self"
        getResult(url, _onSuccess, _onFailure)
    }

    private fun Request.Builder.humtumAuthHeader(multipart: Boolean = false): Request.Builder {
        this.addHeader( "UserAuth", "Bearer ${credentials.idToken}")
            .addHeader("ClientAuth", "${config.clientid} ${config.clientSecret}")
            .addHeader("Accept", "*/*")
            .addHeader("Host", config.ip.removePrefix("http://").removePrefix("https://"))
            .addHeader("Connection", "keep-alive")
            .addHeader("Cache-Control", "no-cache")
            .addHeader("User-Agent", "HumtumAndroidLib")
        if (multipart) this.addHeader("Content-Type", "multipart/form-data")
        return this
    }

    private fun sendRequest(url: String, data: RequestBody? = null , method: String = "GET"): Request {
        if (config.clientid.isNotEmpty() and config.clientSecret.isNotEmpty()) {
            return Request.Builder().humtumAuthHeader()
                .method(method, data)
                .url(url).build()

        } else {
            throw Exception("Empty client id and/or clientSecret")
        }
    }

    private fun getResult(url: String, _onSuccess: (json: JSONObject) -> Unit, _onFailure: (e: Exception) -> Unit = { e -> throw e }) {
        client.newCall(sendRequest(url)).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _onFailure(e)
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                if (result == null)
                    _onFailure(HumtumException("Response blank"))
                else {
                    try {
                        Log.d(TAG, result)
                        _onSuccess(JSONObject(result))
                    } catch (e: JSONException) {
                        Log.e(TAG, "Handle failure")
                        _onFailure(HumtumException("Failed to parse json response"))
                    }
                }
            }

        })
    }

}