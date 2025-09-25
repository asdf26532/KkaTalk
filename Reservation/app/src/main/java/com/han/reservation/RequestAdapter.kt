package com.han.reservation

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
        private val tvUserName: TextView = itemView.findViewById(R.id.tvGuideName)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val btnAccept: Button = itemView.findViewById(R.id.btnAccept)
        private val btnReject: Button = itemView.findViewById(R.id.btnReject)

        fun bind(reservation: Reservation) {
            tvUserName.text = reservation.userName
            tvDate.text = "${reservation.date}"

            btnAccept.setOnClickListener { onAccept(reservation.id) }
            btnReject.setOnClickListener { onReject(reservation.id) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Reservation>() {
        override fun areItemsTheSame(oldItem: Reservation, newItem: Reservation) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Reservation, newItem: Reservation) =
            oldItem == newItem
    }
}