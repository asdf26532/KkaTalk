package com.han.kkaTalk

import android.provider.ContactsContract.CommonDataKinds.Nickname

data class ChatPreview(
    val userName: String,
    val userUid: String,
    val lastMessage: String?
)

