package com.demo.linhthoang.securitypoke

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.provider.AuthCallback
import com.auth0.android.result.Credentials
import com.demo.linhthoang.humtum.Humtum
import com.demo.linhthoang.humtum.HumtumAuth
import com.demo.linhthoang.humtum.HumtumConfig
import com.demo.linhthoang.humtum.HumtumCredential
import okhttp3.OkHttpClient
import org.json.JSONObject


val TAG = "Linh"
class MainActivity : AppCompatActivity() {
    var credentials: Credentials? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        HumtumAuth(this).login(this,
            { it.getSelf(
                { Log.d(TAG, it.toString()) },
                { Log.e(TAG, it.toString()) })
            },
            { Log.e(TAG, it.toString())},
            HumtumConfig(ip = "http://de06b7b9.ngrok.io"))

    }
}
