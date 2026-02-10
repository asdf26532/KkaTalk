package com.han.tripnote.data.local.converter

import androidx.room.TypeConverter
import com.han.tripnote.data.model.TripStatus

class TripStatusConverter {

    @TypeConverter
    fun fromStatus(status: TripStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): TripStatus = TripStatus.valueOf(value)
}