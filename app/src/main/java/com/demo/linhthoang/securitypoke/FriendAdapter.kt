package com.demo.linhthoang.securitypoke

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FriendAdapter(var model: AppViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class FriendViewHolder(val textView: View) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_info, parent, false)
        return FriendViewHolder(view)
    }

    override fun getItemCount(): Int = model.users.value?.size ?: 0
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? FriendViewHolder)?.let { viewHolder ->
            model.users.value?.let { data ->
                viewHolder.textView.findViewById<TextView>(R.id.textView).text = data[position].name

            }
        }
    }

}
