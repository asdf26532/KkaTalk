package com.han.tripnote.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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

            private val tvTitle: TextView = itemView.findViewById(R.id.tvTripTitle)
            private val tvLocation: TextView = itemView.findViewById(R.id.tvTripLocation)
            private val tvDate: TextView = itemView.findViewById(R.id.tvTripDate)
            private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

            fun bind(trip: Trip) {
                tvTitle.text = trip.title
                tvLocation.text = trip.location
                tvDate.text = "${trip.startDate} ~ ${trip.endDate}"
                tvStatus.text = trip.status.displayText()

                val colorRes = when (trip.status) {
                    TripStatus.UPCOMING -> R.color.status_upcoming
                    TripStatus.ONGOING -> R.color.status_ongoing
                    TripStatus.COMPLETED -> R.color.status_completed
                }

                itemView.setOnClickListener {
                    onItemClick(trip)
                }

                itemView.setOnLongClickListener {
                    onItemLongClick(trip)
                    true
                }
            }
        }
    }

