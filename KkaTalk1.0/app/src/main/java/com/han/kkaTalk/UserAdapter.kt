package com.han.kkaTalk

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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

        /*// 아이템 클릭 이벤트
        holder.itemView.setOnClickListener {
            // 사용자 정보를 ChatActivity로 전달
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", currentUser.name)
            intent.putExtra("nick", currentUser.nick)
            intent.putExtra("uId", currentUser.uId)

            context.startActivity(intent)

        }*/
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
    }
}
