package com.han.tripnote

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.han.tripnote.databinding.ItemHistoryBinding
import java.time.LocalDate

class TravelHistoryAdapter(
    private val onClick: (TravelHistory) -> Unit,
    private val onLongClick: (TravelHistory) -> Unit,
    private val isSelected: (TravelHistory) -> Boolean
) : RecyclerView.Adapter<TravelHistoryAdapter.VH>() {

    private val items = mutableListOf<TravelHistory>()

    fun submit(list: List<TravelHistory>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun refreshSelection() = notifyDataSetChanged()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class VH(private val b: ItemHistoryBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(t: TravelHistory) {
            b.tvCity.text = t.city
            b.tvRating.text = "⭐ ${t.rating}"
            b.tvStatus.text = statusText(t)
            b.ivFavorite.visibility =
                if (t.isFavorite) View.VISIBLE else View.GONE

            b.root.isSelected = isSelected(t)

            b.root.setOnClickListener { onClick(t) }
            b.root.setOnLongClickListener {
                onLongClick(t)
                true
            }
        }

        private fun statusText(t: TravelHistory): String {
            val today = LocalDate.now()
            val s = LocalDate.parse(t.startDate)
            val e = LocalDate.parse(t.endDate)
            return when {
                today.isBefore(s) -> "예정"
                today.isAfter(e) -> "종료"
                else -> "진행중"
            }
        }
    }
}
