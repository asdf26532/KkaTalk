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
    val imageUrls: List<String>
)
{
    constructor(): this("", "","","","", "","","", listOf())
}

