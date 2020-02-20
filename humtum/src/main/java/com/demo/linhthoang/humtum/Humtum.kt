package com.demo.linhthoang.humtum

import android.util.Log
import com.vinted.actioncable.client.kotlin.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.net.URI

@Serializable
data class Data(val relationship_request: Map<String, String>)


val TAG = "Humtum"

open class Humtum internal constructor(
    private val config: HumtumConfig,
    private val credentials: HumtumCredential
) {

    private val baseUrl = "${config.ip}${config.apiUrl}"
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        })
        .build()

    private val uri = URI("ws://${config.websocket}/cable")
    private val options = Consumer.Options().apply {
        this.connection.headers = mapOf(
            "Sec-WebSocket-Protocol" to "actioncable-v1-json, actioncable-unsupported, ${credentials.idToken}",
            "Origin" to "http://localhost:3000",
            "Sec-WebSocket-Extensions" to "permessage-deflate; client_max_window_bits"
        )
        this.connection.okHttpClientFactory = {
            OkHttpClient.Builder()
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.HEADERS
                    }).build()
        }
    }

    private val consumer = ActionCable.createConsumer(uri, options)
    private val messageChannel = Channel("MessagesChannel")

    fun subscribeToMessageChannel(
        onConnected: ConnectedHandler?,
        onDisconnected: DisconnectedHandler?,
        onReceived: ReceivedHandler?,
        onFailed: FailedHandler? = null,
        onRejected: RejectedHandler? = null
    ) {
        val subscription = consumer.subscriptions.create(messageChannel)
        subscription.onConnected = onConnected
        subscription.onDisconnected = onDisconnected
        subscription.onFailed = onFailed
        subscription.onReceived = onReceived
        subscription.onRejected = onRejected
        consumer.connect()
    }


    fun getSelf(
        _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) {
        val url = "${baseUrl}users/self"
        getResult(url, _onSuccess, _onFailure)
    }

    fun getFriends(
        appId: String, _onSuccess: (string: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) {
        getAppData(appId, "friends", _onSuccess, _onFailure)
    }

    fun getFriendRequests(
        appId: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) {
        getAppData(appId, "friend_requests", _onSuccess, _onFailure)
    }

    fun getDevelopers(
        appId: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) {
        getAppData(appId, "developers", _onSuccess, _onFailure)
    }

    fun getUsers(
        appId: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) {
        getAppData(appId, "users", _onSuccess, _onFailure)
    }

    fun getAppUser(
        appId: String, dataPath: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) {
        val url = "${baseUrl}apps/$appId/user/$dataPath"
        getResult(url, _onSuccess, _onFailure)
    }

    fun addFriend(
        appID: String, friendID: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) {
        putRelRequest(appID, friendID, "add_friend", emptyMap(), _onSuccess, _onFailure)
    }

    fun unFriend(
        appID: String, friendID: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) {
        putRelRequest(appID, friendID, "unfriend", emptyMap(), _onSuccess, _onFailure)

    }

    fun approveFriendRequest(
        appID: String, friendID: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) {
        relRequestResponse(appID, friendID, "friend", "approve", _onSuccess, _onFailure)
    }

    fun rejectFriendRequest(
        appID: String, friendID: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) {
        relRequestResponse(appID, friendID, "friend", "reject", _onSuccess, _onFailure)
    }

    private val defaultE = { e: java.lang.Exception -> throw e }
    private val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
    private val json = Json(JsonConfiguration.Stable)


    private fun getAppData(
        appId: String, dataPath: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) {
        val url = "${baseUrl}apps/$appId/$dataPath"
        Log.d(TAG, url)
        getResult(url, _onSuccess, _onFailure)
    }

    private fun putRelRequest(
        appID: String, friendID: String, type: String,
        data: Map<String, String>,
        _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit
    ) {
        val url = "${baseUrl}relationships/${appID}/${type}/${friendID}"
        val requestBody = json.stringify(Data.serializer(), Data(data))
        client.newCall(sendRequest(url, requestBody.toRequestBody(JSON), "PUT"))
            .enqueue(template(_onSuccess, _onFailure))
    }

    private fun relRequestResponse(
        appID: String, friendID: String,
        friendOrFollow: String,
        response: String,
        _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit
    ) {
        putRelRequest(
            appID, friendID,
            "respond_to_${friendOrFollow}_request",
            mapOf(Pair("response", response)),
            _onSuccess, _onFailure
        )
    }


    private fun Request.Builder.humtumAuthHeader(multipart: Boolean = false): Request.Builder {
        this.addHeader( "UserAuth", "Bearer ${credentials.idToken}")
            .addHeader("ClientAuth", "${config.clientid} ${config.clientSecret}")
            .addHeader("Accept", "application/json, text/plain, */*")
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

    private fun template(
        _onSuccess: (json: String) -> Unit,
        _onFailure: (e: Exception) -> Unit
    ) = object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            _onFailure(e)
        }

        override fun onResponse(call: Call, response: Response) {
            val result = response.body?.string().toString()
            _onSuccess(result)

        }

    }

    private fun getResult(
        url: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: Exception) -> Unit = defaultE
    ) {
        client.newCall(sendRequest(url)).enqueue(template(_onSuccess, _onFailure))
    }

}