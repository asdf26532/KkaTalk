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
        private val btnViewReview: Button = itemView.findViewById(R.id.btnViewReview)

        fun bind(reservation: Reservation) {
            tvGuideName.text = reservation.guideId
            tvDate.text = reservation.date
            tvStatus.text = reservation.status

            if (reservation.status == "예약 완료") {
                btnViewReview.visibility = View.VISIBLE
                btnViewReview.setOnClickListener {
                    val intent = Intent(itemView.context, ReviewActivity::class.java)
                    intent.putExtra("reservationId", reservation.id)
                    itemView.context.startActivity(intent)
                }
            }

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