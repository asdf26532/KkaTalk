package com.han.reservation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.han.reservation.databinding.ItemTourBinding

class TourAdapter(
    private val tourList: List<Tour>,
    private val onItemClick: (Tour) -> Unit
) : RecyclerView.Adapter<TourAdapter.TourViewHolder>() {

    inner class TourViewHolder(val binding: ItemTourBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TourViewHolder {
        val binding = ItemTourBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TourViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TourViewHolder, position: Int) {
        val tour = tourList[position]
        holder.binding.textTitle.text = tour.title
        holder.binding.textLocation.text = "지역: ${tour.location}"
        holder.binding.textPrice.text = "₩${tour.price}"

        if (tour.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(tour.imageUrl)
                .into(holder.binding.imageTour)
        }

        holder.itemView.setOnClickListener { onItemClick(tour) }
    }

    override fun getItemCount(): Int = tourList.size
}