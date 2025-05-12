package com.han.kkatalk2

data class Guide(
    var title: String,
    var uId: String,
    var nick: String,
    var phoneNumber: String,
    var locate: String,
    var rate: String,
    var content: String,
    var profileImageUrl: String,
    var imageUrls: List<String>,
    var viewCount: Int,
    var timestamp: Long
)
{
    constructor(): this("", "","","","", "","","", listOf(),0,0L)
}

