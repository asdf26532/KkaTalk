package com.han.kkaTalk

data class ChatPreview(
    val userName: String,
    val userNick: String,
    val userUid: String,
    var lastMessage: String?,
    val lastMessageTime: Long
)
{
    constructor():this("","","","",0L)
}

