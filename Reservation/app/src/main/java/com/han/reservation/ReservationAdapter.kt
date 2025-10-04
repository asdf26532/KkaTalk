package com.han.reservation

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class ReservationAdapter(private val onItemClick: (String) -> Unit
) : ListAdapter<Reservation, ReservationAdapter.ReservationViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return ReservationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvGuideName: TextView = itemView.findViewById(R.id.tvGuideName)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        private val btnDetail: Button = itemView.findViewById(R.id.btnDetail)
        private val btnReview: Button = itemView.findViewById(R.id.btnReview)

        fun bind(reservation: Reservation) {
            tvGuideName.text = reservation.guideId
            tvDate.text = reservation.date
            tvStatus.text = reservation.status

            // 기본적으로 버튼 숨김
            btnDetail.visibility = View.GONE
            btnReview.visibility = View.GONE

            when (reservation.status) {
                RequestActivity.STATUS_CONFIRMED -> {
                    btnDetail.visibility = View.VISIBLE
                    btnDetail.setOnClickListener {
                        val context = it.context
                        val intent = Intent(context, DetailActivity::class.java)
                        intent.putExtra("reservationId", reservation.id)
                        context.startActivity(intent)
                    }
                }
                RequestActivity.STATUS_COMPLETED -> {
                    btnReview.visibility = View.VISIBLE
                    btnReview.setOnClickListener {
                        val context = it.context
                        val intent = Intent(context, ReviewActivity::class.java)
                        intent.putExtra("reservationId", reservation.id)
                        context.startActivity(intent)
                    }
                }
            }

            // 리스트 아이템 전체 클릭 → 기본 상세보기 진입
            itemView.setOnClickListener {
                onItemClick(reservation.id)
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