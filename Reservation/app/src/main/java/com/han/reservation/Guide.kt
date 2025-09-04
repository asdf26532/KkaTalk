package com.han.reservation

data class Guide(
    var id: String,
    var name: String,
    var location: String,
    var price: Int,
    var description: String
) {
    constructor() : this("", "", "", 0, "")
}