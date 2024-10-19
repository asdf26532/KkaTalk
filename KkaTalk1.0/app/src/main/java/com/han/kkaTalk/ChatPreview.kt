package com.han.kkaTalk

data class ChatPreview(
    val userName: String,
    val userUid: String,
    var lastMessage: String?
)
{
    constructor():this("","","")
}

