package com.han.tripnote.data.model

enum class TripStatus {
    UPCOMING,   // 예정
    ONGOING,    // 진행중
    COMPLETED;  // 완료

    fun displayText(): String {
        return when (this) {
            UPCOMING -> "예정"
            ONGOING -> "진행중"
            COMPLETED -> "완료"
        }
    }
}