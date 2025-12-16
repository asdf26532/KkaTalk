package com.han.reservation

data class Report(
    var reportId: String,
    var reporterUid: String,
    var accusedUid: String,
    var reason: String,
    var timestamp: Long,
    var guideTitle: String,
    var isHandled: Boolean
){
    constructor():this("","","","",0L,"",false)
}
