package com.han.kkaTalk

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

class MessageAdapter(private val context: Context,
                     private val messageList: ArrayList<Message>,
                     private val profileImageUrl: String?,
                     private val receiverNick: String
                    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val receive = 1

    private val send = 2


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if(viewType == 1) {
            val view: View = LayoutInflater.from(context).inflate(R.layout.receive, parent, false)
            ReceiveViewHolder(view)
        } else {
            val view: View = LayoutInflater.from(context).inflate(R.layout.send, parent, false)
            SendViewHolder(view)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // 현재 메시지
        val currentMessage = messageList[position]
        val dateFormat = SimpleDateFormat("a hh:mm", Locale.getDefault()) // 시간 형식 설정

        // 보내는 데이터
        if(holder.javaClass == SendViewHolder::class.java) {
            val viewHolder = holder as SendViewHolder
            viewHolder.sendMessage.text = currentMessage.message
            viewHolder.sendTime.text = dateFormat.format(currentMessage.timestamp)

            // 읽음 상태 표시
            if (currentMessage.isRead) {
                viewHolder.readStatus.text = " "
            } else {
                viewHolder.readStatus.text = "1"
            }

        } else {
            val viewHolder = holder as ReceiveViewHolder
            holder.nickName.text = receiverNick
            viewHolder.receiveMessage.text = currentMessage.message
            viewHolder.receiveTime.text = dateFormat.format(currentMessage.timestamp)

            // 프로필 이미지를 설정하기 위해 Glide를 사용
            Glide.with(context)
                .load(profileImageUrl) // 전달받은 프로필 이미지 URL 사용
                .placeholder(R.drawable.profile_default) // 기본 이미지 설정
                .into(holder.profileImage) // ReceiveViewHolder의 ImageView에 로드

        }

    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]

        return  if(FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.sendId)){
            send
        } else {
            receive
        }
    }

    class SendViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val sendMessage: TextView = itemView.findViewById(R.id.tv_send_msg)
        val sendTime: TextView = itemView.findViewById(R.id.tv_send_time)
        val readStatus: TextView = itemView.findViewById(R.id.tv_read_status)
    }

    class ReceiveViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val nickName: TextView = itemView.findViewById(R.id.tv_nick)
        val receiveMessage: TextView = itemView.findViewById(R.id.tv_receive_msg)
        val receiveTime: TextView = itemView.findViewById(R.id.tv_receive_time)
        val profileImage: ImageView = itemView.findViewById(R.id.iv_profile)
    }


}