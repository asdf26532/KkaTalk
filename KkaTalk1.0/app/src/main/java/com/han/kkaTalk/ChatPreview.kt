package com.han.kkaTalk

data class ChatPreview(
    val userName: String,
    val userUid: String,
    val lastMessage: String?
)
{
    constructor():this("","","")
}

