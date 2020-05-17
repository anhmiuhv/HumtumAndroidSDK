package com.demo.linhthoang.humtum

import android.content.Context
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient

object HumtumApp {
    //Empty for now, would specify behavior for refresh token maybe?
    var currentInstance: Humtum? = null
    internal set
    internal var account: Auth0? = null
    internal var config: HumtumConfig? = null
    internal var apiClient: AuthenticationAPIClient? = null


    fun initialize(context: Context) {
        val ip = getResourceFromContext(context, "humtum_ip")
            ?: throw HumtumException("IP not specified")
        val websocket = getResourceFromContext(context, "humtum_websocket")
            ?: throw HumtumException("websocket url not specified")
        val apiUrl = getResourceFromContext(context, "humtum_apiUrl") ?: "/"

        config = HumtumConfig("https://$ip", websocket, apiUrl)
        account = Auth0(context).apply {
            isOIDCConformant = true
            apiClient = AuthenticationAPIClient(this)
        }
    }

    internal fun logOut() {
        currentInstance?.also {
            it.logOut()
            currentInstance = null
        }
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