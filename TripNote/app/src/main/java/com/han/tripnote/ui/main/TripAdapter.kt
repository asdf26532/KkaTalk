package com.han.tripnote.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.han.tripnote.R
import com.han.tripnote.data.model.Trip
import com.han.tripnote.data.model.TripStatus

class TripAdapter(
    private val onItemClick: (Trip) -> Unit,
    private val onItemLongClick: (Trip) -> Unit
) : ListAdapter<Trip, TripAdapter.TripViewHolder>(TripDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trip, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

        inner class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
            private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
            private val tvMemo: TextView = itemView.findViewById(R.id.tvMemo)
            private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

            fun bind(trip: Trip) {

                tvTitle.text = trip.title

                tvDate.text = "${trip.startDate} ~ ${trip.endDate}"

                tvMemo.text =
                    if (trip.memo.isNullOrEmpty()) "메모 없음"
                    else trip.memo

                tvStatus.text = trip.status.displayText()

                val colorRes = when (trip.status) {
                    TripStatus.UPCOMING -> R.color.status_upcoming
                    TripStatus.ONGOING -> R.color.status_ongoing
                    TripStatus.COMPLETED -> R.color.status_completed
                }

                tvStatus.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, colorRes)
                )

                // 클릭
                itemView.setOnClickListener {
                    onItemClick(trip)
                }

                // 롱클릭 삭제
                itemView.setOnLongClickListener {
                    onItemLongClick(trip)
                    true
                }
            }
        }

    class TripDiffCallback : DiffUtil.ItemCallback<Trip>() {
        override fun areItemsTheSame(oldItem: Trip, newItem: Trip): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Trip, newItem: Trip): Boolean {
            return oldItem == newItem
        }
    }
}

