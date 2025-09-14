package com.han.reservation

data class Reservation(
    var id: String = "",
    var guideId: String = "",
    var userId: String = "",
    var userName: String = "",
    var contact: String = "",
    var date: String = "",
    var time: String = "",
    var status: String = "",
    var createdAt: Long = System.currentTimeMillis()
)
