package com.han.chattingapp

data class Message(
    val message: String?,
    val sendId: String?
){
    constructor():this("","")
}
