package com.han.kkaTalk

data class Message(
    var message: String?,
    val sendId: String?,
    val receiverId: String?,
    val timestamp: Long?,
    val mread: Boolean? = null,
    var deleted: Boolean? = false,
    var fileUrl: String? = null,
    var reactions: Map<String, String>? = null,
    var Highlighted: Boolean = false
){
    constructor():this("","","",0L, false, false, null, null, false)
}

