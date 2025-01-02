package com.han.kkaTalk

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Locale

class MessageAdapter(private val context: Context,
                     private val messageList: ArrayList<Message>,
                     private val profileImageUrl: String?,
                     private val receiverNick: String,
                     private val receiverUid: String
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
        if (position >= messageList.size) return

        // 현재 메시지
        val currentMessage = messageList[position]
        val dateFormat = SimpleDateFormat("a hh:mm", Locale.getDefault()) // 시간 형식 설정

        // 메시지가 삭제되었는지 확인
        if (currentMessage.deleted == true) {
            // 삭제된 메시지를 표시
            if (holder is SendViewHolder) {
                holder.sendMessage.text = "삭제된 메시지입니다"
                holder.sendTime.text = dateFormat.format(currentMessage.timestamp)
                holder.readStatus.text = if (currentMessage.mread == true) " " else "1"
            } else if (holder is ReceiveViewHolder) {
                holder.nickName.text = receiverNick // 닉네임 유지
                Glide.with(context)
                    .load(profileImageUrl) // 프로필 이미지 유지
                    .placeholder(R.drawable.profile_default)
                    .into(holder.profileImage)
                holder.receiveMessage.text = "삭제된 메시지입니다"
                holder.receiveTime.text = dateFormat.format(currentMessage.timestamp)

                // 프로필이미지 클릭
                profileClick(holder.profileImage)
            }
            return
        }

        // 보내는 데이터
        if(holder.javaClass == SendViewHolder::class.java) {
            val viewHolder = holder as SendViewHolder
            viewHolder.sendMessage.text = currentMessage.message
            viewHolder.sendTime.text = dateFormat.format(currentMessage.timestamp)

            // 읽음 상태 표시
            if (currentMessage.mread == true) {
                viewHolder.readStatus.text = " "
            } else {
                viewHolder.readStatus.text = "1"
            }

            // 메시지 꾹 눌렀을 때 (보내는 메시지에만 설정)
            holder.itemView.setOnLongClickListener {
                (context as ChatActivity).showDeletePopup(currentMessage)
                true
            }

        } else {
            val viewHolder = holder as ReceiveViewHolder
            holder.nickName.text = receiverNick
            viewHolder.receiveMessage.text = currentMessage.message
            viewHolder.receiveTime.text = dateFormat.format(currentMessage.timestamp)

            val reactions = currentMessage.reactions
            if (reactions != null && reactions.containsKey(FirebaseAuth.getInstance().currentUser?.uid)) {
                holder.reactionIcon.text = reactions[FirebaseAuth.getInstance().currentUser?.uid] // 내 반응 표시
            } else {
                holder.reactionIcon.text = ""
            }

            // 반응 추가 클릭 이벤트
            holder.itemView.setOnClickListener {
                (context as ChatActivity).showReactionPopup(currentMessage)
                true // 반응 팝업 메뉴 띄우기
            }

            // 프로필 이미지를 설정하기 위해 Glide를 사용
            Glide.with(context)
                .load(profileImageUrl) // 전달받은 프로필 이미지 URL 사용
                .placeholder(R.drawable.profile_default) // 기본 이미지 설정
                .into(holder.profileImage) // ReceiveViewHolder의 ImageView에 로드

            // 프로필이미지 클릭
            profileClick(holder.profileImage)

        }



    }

    private fun profileClick(imageView: ImageView) {
        imageView.setOnClickListener {
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("nick", receiverNick)
            intent.putExtra("profileImageUrl", profileImageUrl)
            intent.putExtra("uId", receiverUid) // UID 추가 전달
            context.startActivity(intent)
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
        val reactionIcon: TextView = itemView.findViewById(R.id.tv_reactions    )
    }


}