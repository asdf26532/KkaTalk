package com.han.kkaTalk

data class User(
    var name: String,
    var email: String,
    var uId: String,
    var nick: String,
    val profileImageUrl: String,
    var status: String = "offline", // 기본값을 offline으로 설정
    var lastActiveTime: Long?

) {
    constructor(): this("", "", "", "","","offline",0L)
}

