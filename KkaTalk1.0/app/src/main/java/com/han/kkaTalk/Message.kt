package com.han.kkaTalk

data class Message(
    val message: String?,
    val sendId: String?,
    val receiverId: String?
){
    constructor():this("","","")
}

