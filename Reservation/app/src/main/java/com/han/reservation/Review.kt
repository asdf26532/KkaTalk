package com.han.reservation

data class Review(
    var rating: Int,
    var text: String,
    var userId: String,
    var createdAt: Long
) {
    constructor(): this(0, "", "", 0L)
}