package com.han.reservation

data class User(
    var name: String,
    var email: String,
    var uId: String,
    var nick: String,
    var profileImageUrl: String,
    var statusMessage: String?,
    var role: String
){
    constructor(): this("", "", "", "","",null,"user")
}


