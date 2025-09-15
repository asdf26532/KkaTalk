package com.han.reservation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReservationAdapter(private var reservations: List<Reservation>) :
    RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder>()  {

        class ReservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvDate: TextView = itemView.findViewById(R.id.tvDate)
            val tvGuide: TextView = itemView.findViewById(R.id.tvGuide)
            val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list, parent, false)
            return ReservationViewHolder(view)
        }

        override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
            val reservation = reservations[position]
            holder.tvDate.text = "${reservation.date} ${reservation.time}"
            holder.tvGuide.text = "가이드 ID: ${reservation.guideId}"
            holder.tvStatus.text = "상태: ${reservation.status}"
        }

        override fun getItemCount(): Int = reservations.size

        fun updateData(newList: List<Reservation>) {
            reservations = newList
            notifyDataSetChanged()
        }
}