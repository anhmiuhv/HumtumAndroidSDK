package com.demo.linhthoang.humtum

import android.app.Activity
import android.app.Dialog
import com.auth0.android.Auth0Exception
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.authentication.storage.SecureCredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.VoidCallback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials


object HumtumAuth {

    fun launchLoginUI(
        activity: Activity
        , _onSuccess: () -> Unit
        , _onError: (RuntimeException) -> Unit = { e -> throw e }
    ) {
        withNoNulls(
            HumtumApp.account,
            HumtumApp.apiClient,
            HumtumApp.config
        ) { account, apiClient, config ->

            val scopes = arrayOf("openid", "email", "profile")
            WebAuthProvider.login(account)
                .withScheme("demo")
                .withScope(scopes.joinToString(" "))
                .start(activity, object : AuthCallback {
                    override fun onSuccess(credentials: Credentials) {
                        val manager = SecureCredentialsManager(
                            activity,
                            apiClient,
                            SharedPreferencesStorage(activity)
                        )
                        HumtumApp.currentInstance = Humtum(config, manager)
                        _onSuccess()
                    }

                    override fun onFailure(dialog: Dialog) {
                        dialog.show()
                    }

                    override fun onFailure(exception: AuthenticationException?) {
                        _onError(HumtumException("${exception?.description}  (${exception?.code})"))
                    }

                })
        } ?: throw HumtumException("Humtum is not configured")
    }


    fun launchLoginUI(
        activity: Activity
        , audience: String
        , scope: Array<String> = arrayOf("openid", "email", "profile")
        , _onSuccess: () -> Unit
        , _onError: (RuntimeException) -> Unit = { e -> throw e }
    ) {

        withNoNulls(
            HumtumApp.account,
            HumtumApp.apiClient,
            HumtumApp.config
        ) { account, apiClient, config ->
            WebAuthProvider.login(account)
                .withAudience(audience)
                .withScheme("demo")
                .withScope(scope.joinToString(" "))
                .start(activity, object : AuthCallback {
                    override fun onSuccess(credentials: Credentials) {
                        val manager = SecureCredentialsManager(
                            activity,
                            apiClient,
                            SharedPreferencesStorage(activity)
                        )
                        manager.saveCredentials(credentials)
                        HumtumApp.currentInstance = Humtum(config, manager)
                        _onSuccess()
                    }

                    override fun onFailure(dialog: Dialog) {
                        dialog.show()
                    }

                    override fun onFailure(exception: AuthenticationException?) {
                        _onError(HumtumException("${exception?.description}  (${exception?.code})"))
                    }

                })
        } ?: throw HumtumException("Humtum is not configured")


    }

    fun launchLogoutUI(
        activity: Activity, _onSuccess: () -> Unit
        , _onError: (RuntimeException) -> Unit = { e -> throw e }
    ) {
        HumtumApp.account?.let {
            WebAuthProvider.logout(it)
                .withScheme("demo")
                .start(activity, object : VoidCallback {
                    override fun onSuccess(payload: Void?) {
                        _onSuccess()
                        HumtumApp.logOut()
                    }

                    override fun onFailure(error: Auth0Exception?) {
                        _onError(HumtumException("${error?.message}  (${error?.cause})"))
                    }

                })
        }

    }

    private fun <A, B, C> withNoNulls(
        p1: A?,
        p2: B?,
        p3: C?,
        function: (p1: A, p2: B, p3: C) -> Unit
    ): Unit? = p1?.let { p2?.let { p3?.let { function(p1, p2, p3) } } }

}