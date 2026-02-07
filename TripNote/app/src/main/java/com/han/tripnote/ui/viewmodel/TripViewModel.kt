package com.han.tripnote.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.han.tripnote.data.model.Trip


class TripViewModel : ViewModel() {


    // 내부에서만 변경 가능
    private val _tripList = MutableLiveData<List<Trip>>()

    // 외부에는 읽기 전용
    val tripList: LiveData<List<Trip>> get() = _tripList


    init {
    // 임시 더미 데이터
        _tripList.value = listOf(
            Trip("1", "도쿄 여행", "일본 도쿄", "2026.03.01", "2026.03.05"),
            Trip("2", "부산 여행", "대한민국 부산", "2026.04.10", "2026.04.12"),
            Trip("3", "파리 여행", "프랑스 파리", "2026.05.01", "2026.05.08")
        )
    }


    // 여행 추가
    fun addTrip(trip: Trip) {
        val currentList = _tripList.value?.toMutableList() ?: mutableListOf()
        currentList.add(trip)
        _tripList.value = currentList
    }


    // 여행 삭제
    fun deleteTrip(trip: Trip) {
        val currentList = _tripList.value?.toMutableList() ?: return
        currentList.remove(trip)
        _tripList.value = currentList
    }


    // 여행 수정
    fun updateTrip(updatedTrip: Trip) {
        val currentList = _tripList.value?.toMutableList() ?: return
        val index = currentList.indexOfFirst { it.id == updatedTrip.id }
        if (index != -1) {
            currentList[index] = updatedTrip
            _tripList.value = currentList
        }
    }
}