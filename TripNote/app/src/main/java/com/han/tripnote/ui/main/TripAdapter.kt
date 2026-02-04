package com.han.tripnote.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.han.tripnote.R
import com.han.tripnote.data.model.Trip

class TripAdapter(
    private val tripList: MutableList<Trip>,
    private val onLongClick: (Int) -> Unit
) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trip, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(tripList[position])

        holder.itemView.setOnLongClickListener {
            onLongClick(position)
            true
        }
    }

    override fun getItemCount(): Int = tripList.size

    inner class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTitle: TextView = itemView.findViewById(R.id.tvTripTitle)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvTripLocation)
        private val tvDate: TextView = itemView.findViewById(R.id.tvTripDate)

        fun bind(trip: Trip) {
            tvTitle.text = trip.title
            tvLocation.text = trip.location
            tvDate.text = "${trip.startDate} ~ ${trip.endDate}"
        }
    }

    fun submitList(newList: MutableList<Trip>) {
        tripList.clear()
        tripList.addAll(newList)
        notifyDataSetChanged()
    }
}