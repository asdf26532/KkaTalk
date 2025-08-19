package com.han.kkatalk2

data class Booking(
    var bookingId: String? = null,
    var guideId: String? = null,
    var sellerUid: String? = null,
    var buyerUid: String? = null,
    var status: String? = "pending",
    var timestamp: Long? = System.currentTimeMillis()
) {
    constructor() : this("", "", "", "", "pending", System.currentTimeMillis())
}