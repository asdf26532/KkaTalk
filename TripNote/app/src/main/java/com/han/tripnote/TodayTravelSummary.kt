package com.han.tripnote

data class TodayTravelSummary(
    val city: String,
    val dayIndex: Int,
    val places: List<TravelPlace>
)