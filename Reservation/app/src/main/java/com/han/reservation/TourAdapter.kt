package com.han.reservation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TourAdapter (
    private var tourList: List<Tour>,
    private val onItemClick: (Tour) -> Unit,
    private val onEditClick: (Tour) -> Unit,
    private val onDeleteClick: (Tour) -> Unit
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

        // 일반 클릭 → 상세보기
        holder.itemView.setOnClickListener { onItemClick(tour) }

        // 롱클릭 → 수정/삭제 메뉴 표시
        holder.itemView.setOnLongClickListener { view ->
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
            true
        }
    }

    override fun getItemCount(): Int = tourList.size

    fun updateList(newList: List<Tour>) {
        tourList = newList
        notifyDataSetChanged()
    }
}
