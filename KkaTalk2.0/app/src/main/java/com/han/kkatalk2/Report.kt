package com.han.kkatalk2

data class Report(
    val reportId: String,
    val reporterUid: String,
    val accusedUid: String,
    val reason: String,
    val timestamp: Long,
    val guideId: String?,
    val isHandled: Boolean
){
    constructor():this("","","","",0L,null,false)
}
