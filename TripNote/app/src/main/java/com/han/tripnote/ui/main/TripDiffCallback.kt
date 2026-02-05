package com.han.tripnote.ui.main

import androidx.recyclerview.widget.DiffUtil
import com.han.tripnote.data.model.Trip

class TripDiffCallback : DiffUtil.ItemCallback<Trip>() {

    // 같은 아이템인지 (고유값)
    override fun areItemsTheSame(oldItem: Trip, newItem: Trip): Boolean {
        return oldItem.id == newItem.id
    }

    // 내용이 같은지
    override fun areContentsTheSame(oldItem: Trip, newItem: Trip): Boolean {
        return oldItem == newItem
    }
}