package com.han.tripnote

data class TravelHistory(
    val id: String,
    val city: String,
    val startDate: String,
    val endDate: String,
    val rating: Int,
    var isFavorite: Boolean = false,
    var memo: String = ""
)