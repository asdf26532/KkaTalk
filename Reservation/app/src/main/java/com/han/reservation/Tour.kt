package com.han.reservation

data class Tour(
    var id: String,
    var guideId: String,
    var title: String,
    var description: String,
    var location: String,
    var price: Int,
    var imageUrl: String
) {
    constructor() : this("", "", "", "", "", 0, "")
}
