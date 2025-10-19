package com.han.reservation
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

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


    }



}
