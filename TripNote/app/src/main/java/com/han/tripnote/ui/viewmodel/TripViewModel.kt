package com.han.tripnote.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.han.tripnote.data.model.Trip
import com.han.tripnote.data.repository.TripRepository
import com.han.tripnote.ui.trip.TripFilter


class TripViewModel : ViewModel() {

    val tripList: LiveData<List<Trip>> = TripRepository.trips

    private val _filter = MutableLiveData<TripFilter>(TripFilter.ALL)
    val filter: LiveData<TripFilter> = _filter

    private val _filteredTrips = MutableLiveData<List<Trip>>()
    val filteredTrips: LiveData<List<Trip>> = _filteredTrips

    init {
        tripList.observeForever { list ->
            applyFilter(list)
        }
    }

    fun setFilter(filter: TripFilter) {
        _filter.value = filter
        applyFilter(tripList.value ?: emptyList())
    }

    private fun applyFilter(trips: List<Trip>) {

        val currentFilter = _filter.value ?: TripFilter.ALL

        val result = when (currentFilter) {

            is TripFilter.ALL -> trips

            is TripFilter.BY_STATUS ->
                trips.filter { it.status == currentFilter.status }
        }

        _filteredTrips.value = result
    }

    fun upsertTrip(trip: Trip) {
        TripRepository.upsert(trip)
    }

    fun removeTrip(trip: Trip) {
        TripRepository.remove(trip)
    }
}