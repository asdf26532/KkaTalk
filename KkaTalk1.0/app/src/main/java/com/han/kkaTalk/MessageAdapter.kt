package com.han.kkaTalk

import android.content.Context
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
        if (position >= messageList.size) return

        // 현재 메시지
        val currentMessage = messageList[position]
        val dateFormat = SimpleDateFormat("a hh:mm", Locale.getDefault()) // 시간 형식 설정

        // 메시지가 삭제되었는지 확인
        if (currentMessage.deleted) {
            // 삭제된 메시지를 표시
            if (holder is SendViewHolder) {
                holder.sendMessage.text = "삭제된 메시지입니다"
            } else if (holder is ReceiveViewHolder) {
                holder.receiveMessage.text = "삭제된 메시지입니다"
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

        // 메시지 길게 눌렀을 때 삭제 팝업 표시
        holder.itemView.setOnLongClickListener {
            showDeletePopup(currentMessage)
            true
        }

    }

    // 팝업을 띄우고 삭제 처리
    private fun showDeletePopup(message: Message) {
        android.app.AlertDialog.Builder(context)
            .setTitle("메시지 삭제")
            .setMessage("이 메시지를 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                deleteMessage(message)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 메시지 삭제 처리
    private fun deleteMessage(message: Message) {
        val messageRef = FirebaseDatabase.getInstance()
            .reference.child("chats")
            .child(if (FirebaseAuth.getInstance().currentUser?.uid == message.sendId) senderRoom else receiverRoom)
            .child("message")

        message.timestamp?.let {
            messageRef.orderByChild("timestamp").equalTo(it.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (childSnapshot in snapshot.children) {
                            childSnapshot.ref.child("deleted").setValue(true)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("MessageAdapter", "Failed to delete message: ${error.message}")
                    }
                })
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