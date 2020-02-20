package com.demo.linhthoang.humtum

import android.app.Activity
import android.app.Dialog
import android.content.Context
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials


open class HumtumAuth(context: Context) {
    private val account = Auth0(context)
    private val config: HumtumConfig
    private val appId: String

    init {
        val ip = getResourceFromContext(context, "humtum_ip")
            ?: throw HumtumException("IP not specified")
        val websocket = getResourceFromContext(context, "humtum_websocket")
            ?: throw HumtumException("websocket url not specified")
        val apiUrl = getResourceFromContext(context, "humtum_apiUrl") ?: "/"
        val clientId = getResourceFromContext(context, "humtum_clientid")
            ?: throw HumtumException("client id not specified")
        val clientSecret = getResourceFromContext(context, "humtum_clientSecret")
            ?: throw HumtumException("client secret not specified")
        appId = getResourceFromContext(context, "humtum_appid")
            ?: throw HumtumException("appid not specified")
        config = HumtumConfig("https://$ip", websocket, apiUrl, clientId, clientSecret)
    }

    open fun login(activity: Activity
                   , _onSuccess: (Humtum) -> Unit
                   , _onError: (RuntimeException) -> Unit = { e -> throw e }
    ) {
        WebAuthProvider.login(account)
            .withScheme("demo")
            .withScope("openid email profile")
            .start(activity, object: AuthCallback {
                override fun onSuccess(credentials: Credentials) {
                    _onSuccess(Humtum(config, HumtumCredential(credentials)))
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