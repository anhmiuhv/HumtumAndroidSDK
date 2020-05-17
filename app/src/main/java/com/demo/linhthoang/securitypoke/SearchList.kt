package com.demo.linhthoang.securitypoke

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.beust.klaxon.Klaxon
import com.demo.linhthoang.humtum.HumtumManager
import com.demo.linhthoang.securitypoke.Model.FriendRequestMessage
import com.demo.linhthoang.securitypoke.Model.User

fun refreshFriendRequest(model: AppViewModel) {
    HumtumManager.currentInstance?.let {
        it.getFriendRequests(appId, { data ->
            try {
                Log.d(TAG, data)

                val f = Klaxon().parse<FriendRequestMessage>(data)
                model.friendRequest.postValue(f ?: FriendRequestMessage())

            } catch (e: Exception) {
                Log.e(TAG, "Cannot parse friend request")

            }
        })
    }
}

fun refreshFriends(model: AppViewModel) {
    HumtumManager.currentInstance?.let {
        it.getFriends(appId, { data ->
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
    }
}

class SearchList : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)


        view.findViewById<Button>(R.id.button).setOnClickListener { v ->
            try {
                val b = view.findViewById<EditText>(R.id.userid).text.trim().toString().toInt()
                HumtumManager.currentInstance?.addFriend(appId, b.toString(), { str ->
                    Log.d(TAG, str)
                    toast(Toast.makeText(context, "Successful", Toast.LENGTH_LONG))
                    activity?.let {
                        val model = ViewModelProvider(it)[AppViewModel::class.java]
                        refreshFriendRequest(model)
                    }

                }, { Log.e(TAG, it.toString()) })
            } catch (e: NumberFormatException) {
                toast(Toast.makeText(context, "Invalid ID", Toast.LENGTH_LONG))
            }

        }

        return view
    }
}