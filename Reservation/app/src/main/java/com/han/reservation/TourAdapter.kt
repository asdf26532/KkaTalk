package com.han.reservation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TourAdapter (
    private var tourList: List<Tour>,
    private val onItemClick: (Tour) -> Unit
) : RecyclerView.Adapter<TourAdapter.TourViewHolder>() {

    inner class TourViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.textTourTitle)
        val location: TextView = itemView.findViewById(R.id.textTourLocation)
        val date: TextView = itemView.findViewById(R.id.textTourDate)
        val price: TextView = itemView.findViewById(R.id.textTourPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TourViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tour, parent, false)
        return TourViewHolder(view)
    }

    override fun onBindViewHolder(holder: TourViewHolder, position: Int) {
        val tour = tourList[position]
        holder.title.text = tour.title
        holder.location.text = tour.location
        holder.date.text = tour.date
        holder.price.text = "${tour.price}원"

        holder.itemView.setOnClickListener { onItemClick(tour) }
    }

    override fun getItemCount(): Int = tourList.size

    fun updateList(newList: List<Tour>) {
        tourList = newList
        notifyDataSetChanged()
    }
}
