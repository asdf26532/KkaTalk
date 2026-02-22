package com.han.tripnote.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Trip(
    val id: String,
    val title: String,
    val location: String,
    val startDate: String,
    val endDate: String,
    val status: TripStatus = TripStatus.UPCOMING,
    val memo: String? = null,
    val imageUri: String? = null

) : Parcelable