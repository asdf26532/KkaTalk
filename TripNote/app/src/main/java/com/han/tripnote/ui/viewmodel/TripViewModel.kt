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
import androidx.lifecycle.map
import com.han.tripnote.data.model.TripStatus


class TripViewModel : ViewModel() {

    val tripList: LiveData<List<Trip>> = TripRepository.trips

    private val _filter = MutableLiveData<TripFilter>(TripFilter.ALL)
    val filter: LiveData<TripFilter> = _filter

    private val _filteredTrips = MediatorLiveData<List<Trip>>()
    val filteredTrips: LiveData<List<Trip>> = _filteredTrips

    private val _sort = MutableLiveData<TripSort>(TripSort.NEWEST)
    val sort: LiveData<TripSort> = _sort

    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    val upcomingCount: LiveData<Int> = tripList.map { list ->
        list.count { it.status == TripStatus.UPCOMING }
    }

    val ongoingCount: LiveData<Int> = tripList.map { list ->
        list.count { it.status == TripStatus.ONGOING }
    }

    val completedCount: LiveData<Int> = tripList.map { list ->
        list.count { it.status == TripStatus.COMPLETED }
    }

    val totalCount: LiveData<Int> = tripList.map { list ->
        list.size
    }

    init {
        _filteredTrips.addSource(tripList) { applyFilterAndSort() }
        _filteredTrips.addSource(_filter) { applyFilterAndSort() }
        _filteredTrips.addSource(_sort) { applyFilterAndSort() }
        _filteredTrips.addSource(_searchQuery) { applyFilterAndSort() }
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
        val currentQuery = _searchQuery.value?.lowercase() ?: ""

        val filtered = when (currentFilter) {
            is TripFilter.ALL -> trips
            is TripFilter.BY_STATUS ->
                trips.filter { it.status == currentFilter.status }
        }

        val searched = if (currentQuery.isBlank()) {
            filtered
        } else {
            filtered.filter {
                it.title.lowercase().contains(currentQuery) ||
                        it.location.lowercase().contains(currentQuery)
            }
        }

        val sorted = when (currentSort) {
            is TripSort.NEWEST ->
                searched.sortedByDescending { it.id }

            is TripSort.OLDEST ->
                searched.sortedBy { it.id }

            is TripSort.START_DATE_ASC ->
                searched.sortedBy { LocalDate.parse(it.startDate) }

            is TripSort.START_DATE_DESC ->
                searched.sortedByDescending { LocalDate.parse(it.startDate) }
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