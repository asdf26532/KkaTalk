package com.han.tripnote.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.han.tripnote.data.model.Trip

class TripRepository {

    private val _tripList = MutableLiveData<List<Trip>>(emptyList())
    val tripList: LiveData<List<Trip>> get() = _tripList

    fun addTrip(trip: Trip) {
        val current = _tripList.value?.toMutableList() ?: mutableListOf()
        current.add(trip)
        _tripList.value = current
    }

    fun removeTrip(trip: Trip) {
        val current = _tripList.value?.toMutableList() ?: mutableListOf()
        current.remove(trip)
        _tripList.value = current
    }

    fun updateTrip(updatedTrip: Trip) {
        val current = _tripList.value?.toMutableList() ?: mutableListOf()
        val index = current.indexOfFirst { it.id == updatedTrip.id }
        if (index != -1) {
            current[index] = updatedTrip
            _tripList.value = current
        }
    }
}