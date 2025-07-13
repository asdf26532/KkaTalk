package com.han.kkatalk2

data class Report(
    var reportId: String,
    var reporterUid: String,
    var accusedUid: String,
    var reason: String,
    var timestamp: Long,
    var guideId: String?,
    var guideTitle: String,
    var isHandled: Boolean
){
    constructor():this("","","","",0L,null,"",false)
}
