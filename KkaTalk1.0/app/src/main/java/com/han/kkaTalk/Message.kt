package com.han.kkaTalk

data class Message(
    val message: String?,
    val sendId: String?,
    val receiverId: String?,
    val timestamp: Long?,
    var isRead: Boolean = false
){
    constructor():this("","","",0L,false)
}

