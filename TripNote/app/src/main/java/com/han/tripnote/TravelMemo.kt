package com.han.tripnote

data class TravelMemo(
    val id: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)
