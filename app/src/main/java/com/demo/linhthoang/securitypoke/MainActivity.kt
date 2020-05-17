package com.demo.linhthoang.securitypoke

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.auth0.android.result.Credentials
import com.beust.klaxon.Klaxon
import com.demo.linhthoang.humtum.Humtum
import com.demo.linhthoang.humtum.HumtumAuth
import com.demo.linhthoang.humtum.HumtumManager
import com.demo.linhthoang.humtum.HumtumMessage
import com.demo.linhthoang.securitypoke.Model.FriendRequestMessage
import com.demo.linhthoang.securitypoke.Model.MessageData
import com.demo.linhthoang.securitypoke.Model.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.serialization.json.json


const val TAG = "Linh"
const val appId = "4"

fun toast(toast: Toast) {
    val handler = Handler(Looper.getMainLooper())
    handler.post { toast.show() }
}


class MainActivity : AppCompatActivity() {
    var credentials: Credentials? = null
    lateinit var toolbar: ActionBar
    var humtum: Humtum? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        HumtumAuth(this).login(this,
            {
                humtum = HumtumManager.currentInstance
//                humtum?.enrollInApp(appId, {}, {
//                    toast(Toast.makeText(this, "Failed to enroll participants", Toast.LENGTH_LONG))
//                })

                updateData()
            },
            { Log.e(TAG, it.toString()) })

        toolbar = supportActionBar!!
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private fun updateData() {
        val model = ViewModelProvider(this)[AppViewModel::class.java]
        humtum?.let {
            it.getFriends(appId, { data ->
                Log.d(TAG, data)
                try {
                    val f = Klaxon().parseArray<User>(data)?.toTypedArray()
                    val fe = f?.distinct()
                    model.users.postValue(
                        fe?.toTypedArray() ?: emptyArray()
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Cannot parse")
                    model.users.postValue(emptyArray())
                }
            })
            it.getFriendRequests(appId, { data ->
                try {
                    Log.d(TAG, data)

                    val f = Klaxon().parse<FriendRequestMessage>(data)
                    model.friendRequest.postValue(f ?: FriendRequestMessage())

                } catch (e: Exception) {
                    Log.e(TAG, "Cannot parse friend request")

                }
            })
            it.subscribeToMessageChannel({
                Log.d(TAG, "Connected")
                humtum?.createMessage(HumtumMessage("1", "test"
                    , appId,
                    json {
                        "message".to("hello")
                    }, arrayOf(1))
                , { Log.d(TAG, "Success")}
                , { Log.e(TAG, it.toString()) }
                )

            }, {
                Log.d(TAG, "Disconnected")

            }, { mess ->
                try {
                    val f = Klaxon().parse<MessageData>(mess.toString())
                    f?.let {
                        model.messages.postValue(model.messages.value?.plus(f.message))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Cannot parse message")
                }

            }, { err ->
                Log.d(TAG, err.toString())
            }
            )
        }

    }

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.friend_icon -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.friend)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.notification_icon -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.notification)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.search_icon -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.search)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.profile_icon -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.profile)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }
}
