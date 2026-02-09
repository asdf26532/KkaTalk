package com.han.tripnote.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.han.tripnote.data.model.Trip
import com.han.tripnote.data.repository.TripRepository


class TripViewModel : ViewModel() {

    val tripList: LiveData<List<Trip>> = TripRepository.trips

    fun upsertTrip(trip: Trip) {
        TripRepository.upsert(trip)
    }

    fun removeTrip(trip: Trip) {
        TripRepository.remove(trip)
    }
}