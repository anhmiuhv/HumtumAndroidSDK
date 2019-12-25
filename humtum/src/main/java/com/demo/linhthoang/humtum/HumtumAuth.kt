package com.demo.linhthoang.humtum

import android.app.Activity
import android.app.Dialog
import android.content.Context
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import java.lang.RuntimeException


open class HumtumAuth(context: Context) {
    var account = Auth0(context)

    open fun login(activity: Activity, authCallback: AuthCallback) =
        WebAuthProvider.login(account)
            .start(activity, authCallback);

    open fun login(activity: Activity
                   , _onSuccess: (Humtum) -> Unit
                   , _onError: (RuntimeException) -> Unit = { e -> throw e }
                   , config: HumtumConfig = HumtumConfig()
    ) {
        WebAuthProvider.login(account)
            .start(activity, object: AuthCallback {
                override fun onSuccess(credentials: Credentials) {
                    _onSuccess(Humtum(config, HumtumCredential(credentials)))
                }

                override fun onFailure(dialog: Dialog) {
                    dialog.show()
                }

                override fun onFailure(exception: AuthenticationException?) {
                    _onError(HumtumException(exception.toString()))
                }

            })
    }


    private fun getResourceFromContext(
        context: Context,
        resName: String
    ): String? {
        val stringRes =
            context.resources.getIdentifier(resName, "string", context.packageName)
        require(stringRes != 0) {
            String.format(
                "The 'R.string.%s' value it's not defined in your project's resources file.",
                resName
            )
        }
        return context.getString(stringRes)
    }


}