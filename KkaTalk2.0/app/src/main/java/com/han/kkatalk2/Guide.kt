package com.han.kkatalk2

data class Guide(
    var title: String,
    val uId: String,
    val nick: String,
    var phoneNumber: String,
    var locate: String,
    var rate: String,
    var content: String,
    val profileImageUrl: String,
    var imageUrls: List<String>,
    var viewCount: Int,
    var timestamp: Long?
)
{
    constructor(): this("", "","","","", "","","", listOf(),0,0L)
}

