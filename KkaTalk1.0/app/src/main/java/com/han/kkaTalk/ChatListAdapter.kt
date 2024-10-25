package com.han.kkaTalk

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatListAdapter(
    private val chatList: ArrayList<ChatPreview>,
    private val onItemClick: (ChatPreview) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chatlist_layout, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatPreview = chatList[position]
        holder.bind(chatPreview)
        holder.itemView.setOnClickListener {
            onItemClick(chatPreview)
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userNameTextView: TextView = itemView.findViewById(R.id.tv_user_name)
        private val lastMessageTextView: TextView = itemView.findViewById(R.id.tv_last_message)
        private val lastMessageTimeTextView: TextView = itemView.findViewById(R.id.tv_last_message_time)

        fun bind(chatPreview: ChatPreview) {
            userNameTextView.text = chatPreview.userNick
            lastMessageTextView.text = chatPreview.lastMessage

            val formattedTime = SimpleDateFormat("a hh:mm", Locale.getDefault()).format(Date(chatPreview.lastMessageTime))
            lastMessageTimeTextView.text = formattedTime
        }
    }
}
