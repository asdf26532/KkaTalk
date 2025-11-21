package com.han.reservation

data class Message(
    var message: String?,
    var sendId: String?,
    var receiverId: String?,
    var timestamp: Long?,
    var mread: Boolean? = null,
    var deleted: Boolean? = false,
    var fileUrl: String? = null,
    var reactions: Map<String, String>? = null,
    var highlighted: Boolean = false
){
    constructor():this("","","",0L, false, false, null, null, false)
}

