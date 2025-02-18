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

        // 텍스트 및 이미지 메시지 초기화
        if (holder is SendViewHolder) {
            holder.sendMessage.visibility = View.GONE
            holder.itemView.findViewById<ImageView>(R.id.iv_send_image).visibility = View.GONE
        } else if (holder is ReceiveViewHolder) {
            holder.receiveMessage.visibility = View.GONE
            holder.itemView.findViewById<ImageView>(R.id.iv_receive_image).visibility = View.GONE
        }

        // 메시지가 삭제되었는지 확인
        if (currentMessage.deleted == true) {
            // 삭제된 메시지를 표시
            if (holder is SendViewHolder) {
                holder.sendMessage.text = "삭제된 메시지입니다"
                holder.sendMessage.visibility = View.VISIBLE
                holder.sendTime.text = dateFormat.format(currentMessage.timestamp)
                holder.readStatus.text = if (currentMessage.mread == true) " " else "1"
            } else if (holder is ReceiveViewHolder) {
                holder.nickName.text = receiverNick // 닉네임 유지
                Glide.with(context)
                    .load(profileImageUrl) // 프로필 이미지 유지
                    .placeholder(R.drawable.profile_default)
                    .into(holder.profileImage)
                holder.receiveMessage.text = "삭제된 메시지입니다"
                holder.receiveMessage.visibility = View.VISIBLE
                holder.receiveTime.text = dateFormat.format(currentMessage.timestamp)
                // 프로필이미지 클릭
                profileClick(holder.profileImage)
            }
            return
        }

        // 이미지 메시지 처리
        if (!currentMessage.fileUrl.isNullOrEmpty()) {
            if (holder is SendViewHolder) {
                holder.sendMessage.text = currentMessage.message
                holder.sendTime.text = dateFormat.format(currentMessage.timestamp)

                // 보낸 이미지 메시지
                Glide.with(context)
                    .load(currentMessage.fileUrl)
                    .placeholder(R.drawable.profile_default) // 로딩 중
                    .error(R.drawable.profile_default) // 실패 시
                    .into(holder.itemView.findViewById(R.id.iv_send_image))

                holder.itemView.findViewById<ImageView>(R.id.iv_send_image).visibility = View.VISIBLE
                holder.readStatus.text = if (currentMessage.mread == true) " " else "1"

                // 메시지 꾹 눌렀을 때 (보내는 메시지에만 설정)
                holder.itemView.setOnLongClickListener {
                    (context as ChatActivity).showOptionsPopup(currentMessage)
                    true
                }


            } else if (holder is ReceiveViewHolder) {
                holder.nickName.text = receiverNick
                holder.receiveMessage.text = currentMessage.message
                holder.receiveTime.text = dateFormat.format(currentMessage.timestamp)
                // 받은 이미지 메시지
                Glide.with(context)
                    .load(currentMessage.fileUrl)
                    .placeholder(R.drawable.profile_default)
                    .error(R.drawable.profile_default)
                    .into(holder.itemView.findViewById(R.id.iv_receive_image))


                val imageView = holder.itemView.findViewById<ImageView>(R.id.iv_receive_image)
                imageView.visibility  = View.VISIBLE

                Glide.with(context)
                    .load(profileImageUrl)
                    .placeholder(R.drawable.profile_default)
                    .into(holder.profileImage)

                profileClick(holder.profileImage)

                // 리액션 설정
                holder.reactionIcon.text = ""
                holder.reactionIcon.visibility = View.GONE
                val userReaction = currentMessage.reactions?.get(FirebaseAuth.getInstance().currentUser?.uid)
                if (!userReaction.isNullOrEmpty()) {
                    holder.reactionIcon.text = userReaction
                    holder.reactionIcon.visibility = View.VISIBLE
                }

                imageView.setOnLongClickListener {
                    (context as ChatActivity).showReactionPopup(currentMessage)
                    true
                }

                imageView.setOnClickListener {
                    val intent = Intent(context, FullScreenImageActivity::class.java)
                    intent.putExtra("IMAGE_URL", currentMessage.fileUrl) // 이미지 URL 전달
                    context.startActivity(intent)
                }
            }
            return
        }

        // 텍스트 메시지 처리
        if (holder is SendViewHolder) {
            holder.sendMessage.text = currentMessage.message
            holder.sendMessage.visibility = View.VISIBLE
            holder.sendTime.text = dateFormat.format(currentMessage.timestamp)
            holder.readStatus.text = if (currentMessage.mread == true) " " else "1"

            // 메시지 꾹 눌렀을 때 (보내는 메시지에만 설정)
            holder.itemView.setOnLongClickListener {
                (context as ChatActivity).showOptionsPopup(currentMessage)
                true
            }


        } else if (holder is ReceiveViewHolder) {
            holder.nickName.text = receiverNick
            holder.receiveMessage.text = currentMessage.message
            holder.receiveMessage.visibility = View.VISIBLE
            holder.receiveTime.text = dateFormat.format(currentMessage.timestamp)

            // 리액션 설정
            holder.reactionIcon.text = ""
            holder.reactionIcon.visibility = View.GONE
            val userReaction = currentMessage.reactions?.get(FirebaseAuth.getInstance().currentUser?.uid)
            if (!userReaction.isNullOrEmpty()) {
                holder.reactionIcon.text = userReaction
                holder.reactionIcon.visibility = View.VISIBLE
            }
            holder.itemView.setOnLongClickListener {
                (context as ChatActivity).showReactionPopup(currentMessage)
                true
            }
            Glide.with(context)
                .load(profileImageUrl)
                .placeholder(R.drawable.profile_default)
                .into(holder.profileImage)
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
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        return  if(currentUserUid == currentMessage.sendId){
            send
        } else {
            receive
        }
    }

    fun updateList(newList: List<Message>) {
        (messageList as? ArrayList<Message>)?.apply {
            clear()
            addAll(newList)
        }
        notifyDataSetChanged()
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
        val reactionIcon: TextView = itemView.findViewById(R.id.tv_reactions)
    }


}