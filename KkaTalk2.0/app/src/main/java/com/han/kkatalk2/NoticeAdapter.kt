package com.han.kkatalk2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoticeAdapter(
    private val noticeList: List<Notice>,
    private val onItemClick: (Notice) -> Unit
) : RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder>() {

    inner class NoticeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvNoticeTitle)
        val tvDate: TextView = itemView.findViewById(R.id.tvNoticeDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notice, parent, false)
        return NoticeViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        val notice = noticeList[position]
        holder.tvTitle.text = notice.title
        holder.tvDate.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date(notice.timestamp))
        holder.itemView.setOnClickListener {
            onItemClick(notice)
        }
    }

    override fun getItemCount(): Int = noticeList.size
}