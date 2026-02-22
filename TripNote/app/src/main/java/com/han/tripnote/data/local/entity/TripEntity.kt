package com.han.tripnote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.han.tripnote.data.model.TripStatus

@Entity(tableName = "trip")
data class TripEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val location: String,
    val startDate: String,
    val endDate: String,
    val status: TripStatus,
    val imageUri: String? = null
)