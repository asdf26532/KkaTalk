package com.han.tripnote.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.han.tripnote.data.model.Trip
import com.han.tripnote.data.repository.TripRepository


class TripViewModel : ViewModel() {

    private val _tripList = MutableLiveData<List<Trip>>(emptyList())
    val tripList: LiveData<List<Trip>> = _tripList

    fun upsertTrip(trip: Trip) {
        val currentList = _tripList.value?.toMutableList() ?: mutableListOf()
        val index = currentList.indexOfFirst { it.id == trip.id }

        if (index >= 0) {
            // 수정
            currentList[index] = trip
        } else {
            // 추가
            currentList.add(trip)
        }

        _tripList.value = currentList
    }

    fun removeTrip(trip: Trip) {
        val currentList = _tripList.value?.toMutableList() ?: return
        currentList.removeAll { it.id == trip.id }
        _tripList.value = currentList
    }
}