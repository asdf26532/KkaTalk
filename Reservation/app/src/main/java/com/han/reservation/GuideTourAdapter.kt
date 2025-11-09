package com.han.reservation

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.han.reservation.databinding.ItemGuideTourBinding

class GuideTourAdapter(
    private val tours: MutableList<Tour>,
    private val onEditClick: (Tour) -> Unit,
    private val onDeleteClick: (Tour) -> Unit,
    private val onItemClick: (Tour) -> Unit
) : RecyclerView.Adapter<GuideTourAdapter.TourViewHolder>() {

    inner class TourViewHolder(private val binding: ItemGuideTourBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tour: Tour) {
            binding.tvTitle.text = tour.title
            binding.tvLocation.text = tour.location
            binding.tvDate.text = tour.date

            // 항목 클릭 시 상세보기
            binding.root.setOnClickListener { onItemClick(tour) }

            // 점 3개 메뉴 클릭 시 팝업 메뉴 표시
            binding.btnMenu.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.menuInflater.inflate(R.menu.guide_tour_item, popup.menu)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_edit -> {
                            onEditClick(tour)
                            true
                        }
                        R.id.action_delete -> {
                            onDeleteClick(tour)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
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
