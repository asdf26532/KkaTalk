package com.han.tripnote

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.han.tripnote.databinding.ItemMemoBinding

class MemoAdapter(
    private var items: List<TravelMemo>
) : RecyclerView.Adapter<MemoAdapter.VH>() {

    inner class VH(val binding: ItemMemoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemMemoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.tvMemo.text = items[position].content
    }

    override fun getItemCount() = items.size

    fun submit(newItems: List<TravelMemo>) {
        items = newItems
        notifyDataSetChanged()
    }
}