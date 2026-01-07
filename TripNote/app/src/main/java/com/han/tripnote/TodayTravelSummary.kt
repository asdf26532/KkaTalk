package com.han.tripnote

import java.time.LocalDate

data class TodayTravelSummary(
    val city: String,
    val dayIndex: Int,
    val date: LocalDate,
    val places: List<TravelPlace>
)