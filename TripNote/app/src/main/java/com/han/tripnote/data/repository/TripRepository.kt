package com.han.tripnote.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.han.tripnote.data.local.TripDatabase
import com.han.tripnote.data.local.entity.TripEntity
import com.han.tripnote.data.model.Trip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object TripRepository {

    private val _trips = MutableLiveData<List<Trip>>()
    val trips: LiveData<List<Trip>> get() = _trips

    private lateinit var db: TripDatabase

    fun init(context: Context) {
        db = TripDatabase.getInstance(context)
        loadTrips()
    }

    private fun loadTrips() {
        CoroutineScope(Dispatchers.IO).launch {
            val entities = db.tripDao().getAll()
            val list = entities.map { it.toTrip() }
            _trips.postValue(list)
        }
    }

    fun upsert(trip: Trip) {
        CoroutineScope(Dispatchers.IO).launch {
            db.tripDao().upsert(trip.toEntity())
            loadTrips()
        }
    }

    fun remove(trip: Trip) {
        CoroutineScope(Dispatchers.IO).launch {
            db.tripDao().delete(trip.toEntity())
            loadTrips()
        }
    }
}

private fun TripEntity.toTrip() = Trip(
    id, title, location, startDate, endDate
)

private fun Trip.toEntity() = TripEntity(
    id, title, location, startDate, endDate
)