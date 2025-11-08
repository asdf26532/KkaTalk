package com.han.reservation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.han.reservation.databinding.ItemGuideTourBinding

class GuideTourAdapter(
    private val tours: List<Tour>,
    private val onItemClick: (Tour) -> Unit
) : RecyclerView.Adapter<GuideTourAdapter.TourViewHolder>() {

    inner class TourViewHolder(private val binding: ItemGuideTourBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(tour: Tour) {
            binding.tvTitle.text = tour.title
            binding.tvLocation.text = tour.location
            binding.tvDate.text = tour.date
            binding.root.setOnClickListener { onItemClick(tour) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TourViewHolder {
        val binding = ItemGuideTourBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TourViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TourViewHolder, position: Int) {
        holder.bind(tours[position])
    }

    override fun getItemCount() = tours.size
}