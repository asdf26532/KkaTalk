package com.han.reservation

data class Tour(
    var id: String,
    var guideId: String,
    var title: String,
    var description: String,
    var price: Int,
    var location: String,
    var date: String
) {
    constructor() : this("", "", "", "", 0, "", "")
}