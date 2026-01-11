package com.han.tripnote

import java.time.LocalDate

data class TravelDate(
    val startDate: LocalDate,
    val endDate: LocalDate
) {

    fun status(today: LocalDate): TravelStatus {
        return when {
            today.isBefore(startDate) -> TravelStatus.BEFORE
            today.isAfter(endDate) -> TravelStatus.FINISHED
            else -> TravelStatus.ONGOING
        }
    }

    fun dayIndex(today: LocalDate): Int {
        return startDate.until(today).days + 1
    }
}