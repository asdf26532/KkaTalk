package com.han.reservation

data class Reservation(
    var id: String,
    var userId: String,
    var guideId: String,
    var date: String,
    var status: String,
    var createdAt: Long?
) {
    constructor() : this("","","","","",null)
}