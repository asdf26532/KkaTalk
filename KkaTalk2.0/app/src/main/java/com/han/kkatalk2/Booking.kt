package com.han.kkatalk2

data class Booking(
    var bookingId: String,
    var guideUid: String,
    var customerUid: String,
    var guideConfirmed: Boolean,
    var customerConfirmed: Boolean,
    var isCompleted: Boolean,
    var date: String?,
    var createdAt: Long
) {
    constructor() : this("", "", "", false, false, false, "", 0)
}