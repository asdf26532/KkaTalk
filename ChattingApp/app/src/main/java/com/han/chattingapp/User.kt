package com.han.chattingapp

    data class User(
        var name: String,
        var email: String,
        var uId: String

    ) {
        constructor(): this("", "", "")
    }
