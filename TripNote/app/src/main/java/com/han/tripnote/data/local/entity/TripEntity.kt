package com.han.tripnote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip")
data class TripEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val location: String,
    val startDate: String,
    val endDate: String
)