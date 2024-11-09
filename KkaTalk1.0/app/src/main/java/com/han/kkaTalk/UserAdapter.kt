package com.han.kkaTalk

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class UserAdapter(private val context: Context, private val userList: ArrayList<User>) :
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

        // Glide를 사용하여 프로필 이미지를 Firebase Storage에서 불러오기
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

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.tv_name)
        val profileImage: ImageView = itemView.findViewById(R.id.iv_profile_picture)

    }
}
