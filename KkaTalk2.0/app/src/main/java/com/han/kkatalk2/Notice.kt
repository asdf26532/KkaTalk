package com.han.kkatalk2

data class Notice(
    var noticeId: String,
    var title: String,
    var content: String,
    var timestamp: Long
)
{
    constructor(): this("","","",0L)
}
