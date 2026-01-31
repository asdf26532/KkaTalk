package com.han.tripnote.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.han.tripnote.R
import com.han.tripnote.data.model.Trip

class TripAdapter(
    private val tripList: List<Trip>
) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trip, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(tripList[position])
    }

    override fun getItemCount(): Int = tripList.size

    class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTitle: TextView = itemView.findViewById(R.id.tvTripTitle)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvTripLocation)
        private val tvDate: TextView = itemView.findViewById(R.id.tvTripDate)

        // [2일차] 데이터 바인딩
        fun bind(trip: Trip) {
            tvTitle.text = trip.title
            tvLocation.text = trip.location
            tvDate.text = "${trip.startDate} ~ ${trip.endDate}"
        }
    }
}