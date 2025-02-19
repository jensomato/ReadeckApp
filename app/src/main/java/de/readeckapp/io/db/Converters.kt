package de.readeckapp.io.db

import androidx.room.TypeConverter
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it) }
    }

    @TypeConverter
    fun instantToTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilliseconds()
    }

    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        return value?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }

    @TypeConverter
    fun stringListToString(list: List<String>?): String {
        return list?.joinToString(",") ?: ""
    }
}
