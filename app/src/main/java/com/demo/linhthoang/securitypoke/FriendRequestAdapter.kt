package com.demo.linhthoang.securitypoke

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.demo.linhthoang.humtum.HumtumApp


class FriendRequestAdapter(private val model: AppViewModel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    class FriendViewHolder(
        val view: View,
        val textView: TextView,
        val approve: Button,
        val reject: Button
    ) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_request_info, parent, false)
        return FriendViewHolder(
            view,
            view.findViewById(R.id.textView),
            view.findViewById(R.id.approve),
            view.findViewById(R.id.reject)
        )
    }

    override fun getItemCount(): Int =
        (model.friendRequest.value?.received?.size ?: 0) + (model.messages.value?.size ?: 0)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? FriendViewHolder)?.let { viewHolder ->
            if (position < model.friendRequest.value?.received?.size ?: 0) {
                model.friendRequest.value?.let { data ->
                    viewHolder.textView.text =
                        data.received[position].sender?.name
                    viewHolder.approve.setOnClickListener {
                        data.received[position].sender?.id?.let { id ->
                            HumtumApp.currentInstance?.approveFriendRequest(appId, id.toString(), {

                                toast(
                                    Toast.makeText(
                                        viewHolder.view.context,
                                        "Approved",
                                        Toast.LENGTH_LONG
                                    )
                                )
                                refreshFriendRequest(model)
                                refreshFriends(model)
                            }, {
                                toast(
                                    Toast.makeText(
                                        viewHolder.view.context,
                                        "Failed",
                                        Toast.LENGTH_LONG
                                    )
                                )
                            })
                        }
                    }
                    viewHolder.reject.setOnClickListener {
                        data.received[position].sender?.id?.let { id ->
                            HumtumApp.currentInstance?.rejectFriendRequest(appId, id.toString(), {

                                toast(
                                    Toast.makeText(
                                        viewHolder.view.context,
                                        "Approved",
                                        Toast.LENGTH_LONG
                                    )
                                )
                                refreshFriendRequest(model)
                                refreshFriends(model)
                            }, {
                                toast(
                                    Toast.makeText(
                                        viewHolder.view.context,
                                        "Failed",
                                        Toast.LENGTH_LONG
                                    )
                                )
                            })

                        }

                    }
                }
            } else
                model.messages.value?.let { data ->
                    viewHolder.view.findViewById<TextView>(R.id.textView).text =
                        data[position - (model.friendRequest.value?.received?.size ?: 0)]
                    viewHolder.view.findViewById<Button>(R.id.approve).visibility = View.INVISIBLE
                    viewHolder.view.findViewById<Button>(R.id.reject).visibility = View.INVISIBLE
                }
        }
    }

}
