package com.demo.linhthoang.humtum

import android.app.Activity
import android.app.Dialog
import android.content.Context
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.authentication.storage.SecureCredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials


open class HumtumAuth(context: Context) {
    private val account = Auth0(context)
    private val config: HumtumConfig
    private val appId: String
    private val apiClient: AuthenticationAPIClient = AuthenticationAPIClient(account)


    init {
        val ip = getResourceFromContext(context, "humtum_ip")
            ?: throw HumtumException("IP not specified")
        val websocket = getResourceFromContext(context, "humtum_websocket")
            ?: throw HumtumException("websocket url not specified")
        val apiUrl = getResourceFromContext(context, "humtum_apiUrl") ?: "/"
        appId = getResourceFromContext(context, "humtum_appid")
            ?: throw HumtumException("appid not specified")
        config = HumtumConfig("https://$ip", websocket, apiUrl)
    }

    open fun login(activity: Activity
                   , _onSuccess: () -> Unit
                   , _onError: (RuntimeException) -> Unit = { e -> throw e }
    ) {
        val scopes = arrayOf("openid", "email", "profile")
        WebAuthProvider.login(account)
            .withScheme("demo")
            .withScope(scopes.joinToString (" "))
            .start(activity, object: AuthCallback {
                override fun onSuccess(credentials: Credentials) {
                    val manager = SecureCredentialsManager(activity, apiClient, SharedPreferencesStorage(activity))
                    HumtumManager.currentInstance = Humtum(config, manager)
                    _onSuccess()
                }

                override fun onFailure(dialog: Dialog) {
                    dialog.show()
                }

                override fun onFailure(exception: AuthenticationException?) {
                    _onError(HumtumException("${exception?.description}  (${exception?.code})"))
                }

            })
    }

    open fun login(activity: Activity
                   , audience: String
                   , scope: Array<String> = arrayOf("openid", "email", "profile")
                   , _onSuccess: () -> Unit
                   , _onError: (RuntimeException) -> Unit = { e -> throw e }
    ) {
        WebAuthProvider.login(account)
            .withAudience(audience)
            .withScheme("demo")
            .withScope(scope.joinToString (" "))
            .start(activity, object: AuthCallback {
                override fun onSuccess(credentials: Credentials) {
                    val manager = SecureCredentialsManager(activity, apiClient, SharedPreferencesStorage(activity))
                    manager.saveCredentials(credentials)
                    HumtumManager.currentInstance = Humtum(config, manager)
                    _onSuccess()
                }

                override fun onFailure(dialog: Dialog) {
                    dialog.show()
                }

                override fun onFailure(exception: AuthenticationException?) {
                    _onError(HumtumException("${exception?.description}  (${exception?.code})"))
                }

            })
    }


    private fun getResourceFromContext(
        context: Context,
        resName: String
    ): String? {
        val stringRes =
            context.resources.getIdentifier(resName, "string", context.packageName)
        if (stringRes == 0) return null

        return context.getString(stringRes)
    }


}