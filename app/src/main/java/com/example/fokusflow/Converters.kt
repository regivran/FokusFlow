package com.example.fokusflow

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun fromPriority(value: String): Priority {
        return Priority.valueOf(value)
    }

    @TypeConverter
    fun priorityToString(priority: Priority): String {
        return priority.name
    }
}