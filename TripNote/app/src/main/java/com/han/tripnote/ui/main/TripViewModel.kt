package com.han.tripnote.ui.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.han.tripnote.data.model.Trip
import com.han.tripnote.util.TripStorage

class TripViewModel : ViewModel() {

    // 여행 목록 LiveData
    private val _tripList = MutableLiveData<MutableList<Trip>>(mutableListOf())
    val tripList: LiveData<MutableList<Trip>> = _tripList

    // 저장된 데이터 로드
    fun load(context: Context) {
        _tripList.value = TripStorage.load(context).toMutableList()
    }

    // 여행 추가
    fun addTrip(context: Context, trip: Trip) {
        val list = _tripList.value ?: mutableListOf()
        list.add(trip)
        _tripList.value = list

        TripStorage.save(context, list)
    }

    // 여행 삭제
    fun removeTrip(context: Context, position: Int) {
        val list = _tripList.value ?: return
        list.removeAt(position)
        _tripList.value = list

        TripStorage.save(context, list)
    }
}