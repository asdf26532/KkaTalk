package com.han.tripnote.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.han.tripnote.data.model.Trip
import com.han.tripnote.data.repository.TripRepository
import com.han.tripnote.ui.trip.TripFilter
import com.han.tripnote.ui.trip.TripSort
import java.time.LocalDate


class TripViewModel : ViewModel() {

    val tripList: LiveData<List<Trip>> = TripRepository.trips

    private val _filter = MutableLiveData<TripFilter>(TripFilter.ALL)
    val filter: LiveData<TripFilter> = _filter

    private val _filteredTrips = MediatorLiveData<List<Trip>>()
    val filteredTrips: LiveData<List<Trip>> = _filteredTrips

    private val _sort = MutableLiveData<TripSort>(TripSort.NEWEST)
    val sort: LiveData<TripSort> = _sort

    init {
        _filteredTrips.addSource(tripList) { applyFilterAndSort() }
        _filteredTrips.addSource(_filter) { applyFilterAndSort() }
        _filteredTrips.addSource(_sort) { applyFilterAndSort() }
    }

    fun setFilter(filter: TripFilter) {
        _filter.value = filter
    }

    fun setSort(sort: TripSort) {
        _sort.value = sort
    }

    private fun applyFilterAndSort() {

        val trips = tripList.value ?: emptyList()
        val currentFilter = _filter.value ?: TripFilter.ALL
        val currentSort = _sort.value ?: TripSort.NEWEST

        val filtered = when (currentFilter) {
            is TripFilter.ALL -> trips
            is TripFilter.BY_STATUS ->
                trips.filter { it.status == currentFilter.status }
        }

        val sorted = when (currentSort) {

            is TripSort.NEWEST ->
                filtered.sortedByDescending { it.id }

            is TripSort.OLDEST ->
                filtered.sortedBy { it.id }

            is TripSort.START_DATE_ASC ->
                filtered.sortedBy { LocalDate.parse(it.startDate) }

            is TripSort.START_DATE_DESC ->
                filtered.sortedByDescending { LocalDate.parse(it.startDate) }
        }

        _filteredTrips.value = sorted
    }

    fun upsertTrip(trip: Trip) {
        TripRepository.upsert(trip)
    }

    fun removeTrip(trip: Trip) {
        TripRepository.remove(trip)
    }
}