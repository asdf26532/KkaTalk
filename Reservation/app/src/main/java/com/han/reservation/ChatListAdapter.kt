package com.han.reservation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatListAdapter(
    private val chatList: ArrayList<ChatPreview>,
    private val onItemClick: (ChatPreview) -> Unit,
    private val onItemDelete: (ChatPreview) -> Unit // 삭제 기능 추가
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
        holder.itemView.setOnLongClickListener { // 롱클릭으로 삭제 이벤트 처리
            onItemDelete(chatPreview)
            true
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userNameTextView: TextView = itemView.findViewById(R.id.tv_user_name)
        private val lastMessageTextView: TextView = itemView.findViewById(R.id.tv_last_message)
        private val unreadCount: TextView = itemView.findViewById(R.id.tv_unread_count)
        private val lastMessageTimeTextView: TextView = itemView.findViewById(R.id.tv_last_message_time)
        private val profileImageView: ImageView = itemView.findViewById(R.id.iv_profile_picture)

        fun bind(chatPreview: ChatPreview) {
            userNameTextView.text = chatPreview.userNick
            lastMessageTextView.text = chatPreview.lastMessage

            val formattedTime = SimpleDateFormat("a hh:mm", Locale.getDefault()).format(Date(chatPreview.lastMessageTime))
            lastMessageTimeTextView.text = formattedTime

            // Glide로 프로필 이미지 로드
            if (!chatPreview.profileImageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(chatPreview.profileImageUrl)
                    .placeholder(R.drawable.profile_default) // 기본 이미지
                    .into(profileImageView)
            } else {
                profileImageView.setImageResource(R.drawable.profile_default)
            }

            if (chatPreview.unreadCount > 0) {
                unreadCount.visibility = View.VISIBLE
                unreadCount.text = chatPreview.unreadCount.toString()
            } else {
                unreadCount.visibility = View.GONE
            }

        }
    }
}
