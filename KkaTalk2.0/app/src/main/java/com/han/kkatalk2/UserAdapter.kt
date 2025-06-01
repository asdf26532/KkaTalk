package com.han.kkatalk2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class UserAdapter(private val context: Context, private var userList: ArrayList<User>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var onItemClickListener: ((User) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.user_layout, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.nameText.text = currentUser.nick

        // 상태 메시지 추가
        if (!currentUser.statusMessage.isNullOrEmpty()) {
            val maxLength = 15 // 최대 글자 수 제한
            val statusMessage = if (currentUser.statusMessage!!.length > maxLength) {
                currentUser.statusMessage!!.take(maxLength) + "..." // 제한 초과 시 "..." 추가
            } else {
                currentUser.statusMessage
            }
            holder.statusText.text = statusMessage
            holder.statusText.visibility = View.VISIBLE // 상태 메시지가 있으면 보이기
        } else {
            holder.statusText.visibility = View.GONE // 상태 메시지가 없으면 숨기기
        }

        // 프로필 이미지 바인딩
        if (!currentUser.profileImageUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(currentUser.profileImageUrl) // URL 가져오기
                .placeholder(R.drawable.profile_default) // 기본 프로필 이미지 설정
                .into(holder.profileImage) // 프로필 이미지에 적용
        } else {
            holder.profileImage.setImageResource(R.drawable.profile_default) // URL이 없을 때 기본 이미지
        }

        // 아이템 클릭 이벤트
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(currentUser) // 클릭 시 리스너 호출
        }

    }

    // 아이템 클릭 리스너 설정 메서드
    fun setOnItemClickListener(listener: (User) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    // 차단된 사용자 목록만 업데이트
    fun updateList(blockedUserIds: List<String>, allUsers: List<User>) {
        userList = ArrayList(allUsers.filter { blockedUserIds.contains(it.uId) })
        notifyDataSetChanged()
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.tv_name)
        val profileImage: ImageView = itemView.findViewById(R.id.iv_profile_picture)
        val statusText: TextView = itemView.findViewById(R.id.tv_status_message)

    }
}
