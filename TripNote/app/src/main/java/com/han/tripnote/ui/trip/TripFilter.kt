package com.han.tripnote.ui.trip

import com.han.tripnote.data.model.TripStatus

sealed class TripFilter {

    object ALL : TripFilter()
    data class BY_STATUS(val status: TripStatus) : TripFilter()
}