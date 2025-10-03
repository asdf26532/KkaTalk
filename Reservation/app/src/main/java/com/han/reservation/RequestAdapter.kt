package com.han.reservation

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class RequestAdapter(
    private val onAccept: (String) -> Unit,
    private val onReject: (String) -> Unit
) : ListAdapter<Reservation, RequestAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRequesterName: TextView = itemView.findViewById(R.id.tvRequesterName)
        private val tvDate: TextView = itemView.findViewById(R.id.tvRequestDate)

        private val btnAccept: Button = itemView.findViewById(R.id.btnAccept)
        private val btnReject: Button = itemView.findViewById(R.id.btnReject)
        private val btnDetail: Button = itemView.findViewById(R.id.btnDetail)
        private val btnReview: Button = itemView.findViewById(R.id.btnReview)


        fun bind(reservation: Reservation) {
            tvRequesterName.text = reservation.userId
            tvDate.text = "${reservation.date}"

            // 초기화
            btnAccept.visibility = View.GONE
            btnReject.visibility = View.GONE
            btnDetail.visibility = View.GONE
            btnReview.visibility = View.GONE

            when (reservation.status) {
                RequestActivity.STATUS_PENDING -> {
                    btnAccept.visibility = View.VISIBLE
                    btnReject.visibility = View.VISIBLE

                    btnAccept.setOnClickListener { onAccept(reservation.id) }
                    btnReject.setOnClickListener { onReject(reservation.id) }
                }
                RequestActivity.STATUS_CONFIRMED -> {
                    btnDetail.visibility = View.VISIBLE

                    // 상세보기
                    btnDetail.setOnClickListener {
                        val context = it.context
                        val intent = Intent(context, DetailActivity::class.java)
                        intent.putExtra("reservationId", reservation.id)
                        context.startActivity(intent)
                    }
                }
                RequestActivity.STATUS_COMPLETED -> {
                    btnReview.visibility = View.VISIBLE

                    // 후기 
                    btnReview.setOnClickListener {
                        val context = it.context
                        val intent = Intent(context, ReviewActivity::class.java)
                        intent.putExtra("reservationId", reservation.id) // 예약 ID 전달
                        context.startActivity(intent)
                    }
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Reservation>() {
        override fun areItemsTheSame(oldItem: Reservation, newItem: Reservation) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Reservation, newItem: Reservation) =
            oldItem == newItem
    }
}