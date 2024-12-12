package com.han.kkaTalk

data class User(
    var name: String,
    var email: String,
    var uId: String,
    var nick: String,
    val profileImageUrl: String,
    var statusMessage: String? = null

) {
    constructor(): this("", "", "", "","",null)
}

