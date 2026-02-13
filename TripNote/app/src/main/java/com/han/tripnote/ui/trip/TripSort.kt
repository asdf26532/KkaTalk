package com.han.tripnote.ui.trip

sealed class TripSort {

    object NEWEST : TripSort()
    object OLDEST : TripSort()

    object START_DATE_ASC : TripSort()
    object START_DATE_DESC : TripSort()
}