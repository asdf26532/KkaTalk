package com.han.kkatalk2

data class Guide(
    var name: String,
    var email: String,
    var uId: String,
    var phoneNumber: String,
    var content: String,
    var profileImageUrl: String
)
{
    constructor(): this("", "", "", "","", "")
}

