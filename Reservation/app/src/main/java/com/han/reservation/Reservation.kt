package com.han.reservation

data class Reservation(
    var id: String = "",
    var guideId: String = "",
    var userId: String = "testUser01",
    var userName: String = "홍길동",
    var contact: String = "010-1234-5678",
    var date: String = "",
    var time: String = "",
    var status: String = "reserved",
    var createdAt: Long = System.currentTimeMillis()
)