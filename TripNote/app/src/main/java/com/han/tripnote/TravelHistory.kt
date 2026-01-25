package com.han.tripnote

data class TravelHistory(
    val id: String,
    var city: String,
    var startDate: String,
    var endDate: String,
    var rating: Int,
    var isFavorite: Boolean = false,
    var memo: String = ""
)