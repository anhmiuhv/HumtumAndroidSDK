package com.demo.linhthoang.humtum

import android.util.Base64
import android.util.Log
import com.auth0.android.authentication.storage.CredentialsManagerException
import com.auth0.android.authentication.storage.SecureCredentialsManager
import com.auth0.android.callback.BaseCallback
import com.auth0.android.result.Credentials
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


class Humtum internal constructor(
    private val config: HumtumConfig,
    private val manager: SecureCredentialsManager
) {
    val TAG = "Humtum"

    @Serializable
    private data class RelationshipRequestData(val relationship_request: Map<String, String>)

    @Serializable
    private data class ActionCableAuthToken(val id_token: String?, val access_token: String?)


    private val baseUrl = "${config.ip}${config.apiUrl}"
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        })
        .build()

    private val uri = URI("ws://${config.websocket}/cable")
    private val options = { credentials: Credentials ->
        Consumer.Options().apply {
            this.connection.headers = mapOf(
                "Sec-WebSocket-Protocol" to "actioncable-v1-json, actioncable-unsupported, ${generateAuthToken(
                    credentials
                )}",
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
    }

    private val consumer = { credentials: Credentials ->
        ActionCable.createConsumer(uri, options(credentials))
    }

    private val _credentials =
        { func: (Credentials) -> Unit, err: (e: java.lang.Exception) -> Unit ->
            manager.getCredentials(object : BaseCallback<Credentials, CredentialsManagerException> {
                override fun onSuccess(payload: Credentials?) {
                    payload?.also(func)

                }

                override fun onFailure(error: CredentialsManagerException?) {
                    err(java.lang.Exception("CredentialsManagerException ${error?.message}"))
                }

            })
        }
    private val messageChannel = Channel("MessagesChannel")

    fun subscribeToMessageChannel(
        onConnected: ConnectedHandler?,
        onDisconnected: DisconnectedHandler?,
        onReceived: ReceivedHandler?,
        onFailed: FailedHandler? = null,
        onRejected: RejectedHandler? = null,
        onError: (java.lang.Exception) -> Unit = defaultE
    ) = _credentials({
        val subscription = consumer(it).subscriptions.create(messageChannel)
        subscription.onConnected = onConnected
        subscription.onDisconnected = onDisconnected
        subscription.onFailed = onFailed
        subscription.onReceived = onReceived
        subscription.onRejected = onRejected
        consumer(it).connect()
    }, onError)


    fun createMessage(
        message: HumtumMessage
        , _onSuccess: (json: String) -> Unit
        , _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) {
        val url = "/messages"
        val requestBody = json.stringify(HumtumMessage.serializer(), message)
        _credentials({
            client.newCall(sendRequest(it, url, requestBody.toRequestBody(jsonType), "POST"))
                .enqueue(template(_onSuccess, _onFailure))
        }, _onFailure)
    }


    fun receiveMessage(
        id: Long, _onSuccess: (json: String) -> Unit
        , _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) {
        val url = "/messages/${id}/receive"
        _credentials({
            client.newCall(sendRequest(it, url, null, "PUT"))
                .enqueue(template(_onSuccess, _onFailure))
        }, _onFailure)
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
    ) = getAppData(appId, "friends", _onSuccess, _onFailure)

    fun getFriendRequests(
        appId: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) = getAppData(appId, "friend_requests", _onSuccess, _onFailure)

    fun getFollowers(
        appId: String, _onSuccess: (string: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) = getAppData(appId, "followers", _onSuccess, _onFailure)

    fun getFollowing(
        appId: String, _onSuccess: (string: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) = getAppData(appId, "following", _onSuccess, _onFailure)

    fun getFollowerRequests(
        appId: String, _onSuccess: (string: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) = getAppData(appId, "follower_requests", _onSuccess, _onFailure)

    fun getFollowingRequests(
        appId: String, _onSuccess: (string: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) = getAppData(appId, "following_requests", _onSuccess, _onFailure)

    fun getDevelopers(
        appId: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) = getAppData(appId, "developers", _onSuccess, _onFailure)

    fun getUsers(
        appId: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) = getAppData(appId, "users", _onSuccess, _onFailure)

    fun searchUsersInApp(
        appId: String, query: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) = searchAppData(appId, query, "users", _onSuccess, _onFailure)


    fun enrollInApp(
        appId: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) {
        val url = "${baseUrl}apps/${appId}/enroll"
        _credentials({
            client.newCall(sendRequest(it, url, null, "POST"))
                .enqueue(template(_onSuccess, _onFailure))
        }, _onFailure)
    }

    fun unenrollInApp(
        appId: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) {
        val url = "${baseUrl}apps/${appId}/unenroll"
        _credentials({
            client.newCall(sendRequest(it, url, null, "DELETE"))
                .enqueue(template(_onSuccess, _onFailure))
        }, _onFailure)
    }

    fun getAppUser(
        appId: String, uid: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) {
        val url = "${baseUrl}apps/$appId/user/$uid"
        getResult(url, _onSuccess, _onFailure)
    }

    fun addFriend(
        appID: String, friendID: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) = putRelRequest(appID, friendID, "add_friend", emptyMap(), _onSuccess, _onFailure)

    fun unFriend(
        appID: String, friendID: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) = putRelRequest(appID, friendID, "unfriend", emptyMap(), _onSuccess, _onFailure)

    fun followOther(
        appID: String, friendID: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) = putRelRequest(appID, friendID, "follow", emptyMap(), _onSuccess, _onFailure)

    fun unfollow(
        appID: String, friendID: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) = putRelRequest(appID, friendID, "unfollow", emptyMap(), _onSuccess, _onFailure)

    fun approveFriendRequest(
        appID: String, friendID: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) = relRequestResponse(appID, friendID, "friend", "approve", _onSuccess, _onFailure)

    fun rejectFriendRequest(
        appID: String, friendID: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) = relRequestResponse(appID, friendID, "friend", "reject", _onSuccess, _onFailure)

    fun approveFollowRequest(
        appID: String, friendID: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) = relRequestResponse(appID, friendID, "follow", "approve", _onSuccess, _onFailure)

    fun rejectFollowRequest(
        appID: String, friendID: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) = relRequestResponse(appID, friendID, "follow", "reject", _onSuccess, _onFailure)

    internal fun logOut() {
        manager.clearCredentials()
    }

    private val defaultE = { e: java.lang.Exception -> throw e }
    private val jsonType = "application/json; charset=utf-8".toMediaTypeOrNull()
    private val json = Json(JsonConfiguration.Stable)


    private fun getAppData(
        appId: String, dataPath: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) {
        val url = "${baseUrl}apps/$appId/$dataPath"
        Log.d(TAG, url)
        getResult(url, _onSuccess, _onFailure)
    }

    private fun searchAppData(
        appId: String, query: String, dataPath: String, _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit = defaultE
    ) {
        val url = "${baseUrl}apps/$appId/$dataPath?q=${query}"
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
        val requestBody =
            json.stringify(RelationshipRequestData.serializer(), RelationshipRequestData(data))
        _credentials({
            client.newCall(sendRequest(it, url, requestBody.toRequestBody(jsonType), "PUT"))
                .enqueue(template(_onSuccess, _onFailure))
        }, _onFailure)
    }

    private fun relRequestResponse(
        appID: String, friendID: String,
        friendOrFollow: String,
        response: String,
        _onSuccess: (json: String) -> Unit,
        _onFailure: (e: java.lang.Exception) -> Unit
    ) = putRelRequest(
        appID, friendID,
        "respond_to_${friendOrFollow}_request",
        mapOf(Pair("response", response)),
        _onSuccess, _onFailure
    )


    private fun Request.Builder.humtumAuthHeader(
        credentials: Credentials,
        multipart: Boolean = false
    ): Request.Builder {
        this.addHeader("UserAuth", "Bearer ${credentials.idToken}")
            .addHeader("AccessAuth", "Bearer ${credentials.accessToken}")
            .addHeader("Accept", "application/json, text/plain, */*")
            .addHeader("Host", config.ip.removePrefix("http://").removePrefix("https://"))
            .addHeader("Connection", "keep-alive")
            .addHeader("Cache-Control", "no-cache")
            .addHeader("User-Agent", "HumtumAndroidLib")
        if (multipart) this.addHeader("Content-Type", "multipart/form-data")
        return this
    }

    private fun sendRequest(
        credentials: Credentials,
        url: String,
        data: RequestBody? = null,
        method: String = "GET"
    ): Request {
        if (!credentials.idToken.isNullOrBlank() and !credentials.accessToken.isNullOrBlank()) {
            return Request.Builder().humtumAuthHeader(credentials)
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
    ) = _credentials({
        client.newCall(sendRequest(it, url)).enqueue(template(_onSuccess, _onFailure))
    }, _onFailure)

    private fun generateAuthToken(credentials: Credentials): ByteArray? {
        val token = ActionCableAuthToken(credentials.idToken, credentials.accessToken)
        val jwt = json.stringify(ActionCableAuthToken.serializer(), token)
        return Base64.encode(jwt.toByteArray(), Base64.NO_PADDING)
    }
}

