package com.han.tripnote

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.han.tripnote.databinding.ItemHistoryBinding
import com.han.tripnote.databinding.ItemSectionHeaderBinding
import java.time.LocalDate

class TravelHistoryAdapter(
    private val onClick: (TravelHistory) -> Unit,
    private val onLongClick: (TravelHistory) -> Unit,
    private val isSelected: (TravelHistory) -> Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<Row>()

    sealed class Row {
        data class Header(val title: String) : Row()
        data class Item(val history: TravelHistory) : Row()
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    fun submit(list: List<TravelHistory>) {
        items.clear()

        val today = LocalDate.now()

        val upcoming = list.filter {
            today.isBefore(LocalDate.parse(it.startDate))
        }
        val ongoing = list.filter {
            val s = LocalDate.parse(it.startDate)
            val e = LocalDate.parse(it.endDate)
            !today.isBefore(s) && !today.isAfter(e)
        }
        val ended = list.filter {
            today.isAfter(LocalDate.parse(it.endDate))
        }

        if (upcoming.isNotEmpty()) {
            items.add(Row.Header("📅 예정 여행"))
            upcoming.forEach { items.add(Row.Item(it)) }
        }
        if (ongoing.isNotEmpty()) {
            items.add(Row.Header("🚀 진행중 여행"))
            ongoing.forEach { items.add(Row.Item(it)) }
        }
        if (ended.isNotEmpty()) {
            items.add(Row.Header("🏁 종료 여행"))
            ended.forEach { items.add(Row.Item(it)) }
        }

        notifyDataSetChanged()
    }

    fun refreshSelection() = notifyDataSetChanged()

    override fun getItemViewType(position: Int) =
        when (items[position]) {
            is Row.Header -> TYPE_HEADER
            is Row.Item -> TYPE_ITEM
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        if (viewType == TYPE_HEADER) {
            val b = ItemSectionHeaderBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            HeaderVH(b)
        } else {
            val b = ItemHistoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            ItemVH(b)
        }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, pos: Int) {
        when (val row = items[pos]) {
            is Row.Header -> (holder as HeaderVH).bind(row)
            is Row.Item -> (holder as ItemVH).bind(row.history)
        }
    }

    inner class HeaderVH(
        private val b: ItemSectionHeaderBinding
    ) : RecyclerView.ViewHolder(b.root) {
        fun bind(h: Row.Header) {
            b.tvHeader.text = h.title
        }
    }

    inner class ItemVH(
        private val b: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(b.root) {

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
