package com.han.kkaTalk

data class User(
    var name: String,
    var email: String,
    var uId: String,
    var nick: String

) {
    constructor(): this("", "", "", "")
}

