package de.readeckapp.io.db

import androidx.room.TypeConverter
import de.readeckapp.io.db.model.BookmarkEntity
import kotlinx.datetime.Instant

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

    @TypeConverter
    fun fromState(state: BookmarkEntity.State): Int {
        return when (state) {
            BookmarkEntity.State.LOADED -> BookmarkEntity.State.LOADED.value
            BookmarkEntity.State.ERROR -> BookmarkEntity.State.ERROR.value
            BookmarkEntity.State.LOADING -> BookmarkEntity.State.LOADING.value
        }
    }

    @TypeConverter
    fun toState(stateValue: Int): BookmarkEntity.State {
        return when (stateValue) {
            BookmarkEntity.State.LOADED.value -> BookmarkEntity.State.LOADED
            BookmarkEntity.State.ERROR.value -> BookmarkEntity.State.ERROR
            BookmarkEntity.State.LOADING.value -> BookmarkEntity.State.LOADING
            else -> BookmarkEntity.State.ERROR
        }
    }

    @TypeConverter
    fun fromType(state: BookmarkEntity.Type): String {
        return when (state) {
            BookmarkEntity.Type.ARTICLE -> BookmarkEntity.Type.ARTICLE.value
            BookmarkEntity.Type.PHOTO -> BookmarkEntity.Type.PHOTO.value
            BookmarkEntity.Type.VIDEO -> BookmarkEntity.Type.VIDEO.value
        }
    }

    @TypeConverter
    fun toType(stateValue: String): BookmarkEntity.Type {
        return when (stateValue) {
            BookmarkEntity.Type.ARTICLE.value -> BookmarkEntity.Type.ARTICLE
            BookmarkEntity.Type.PHOTO.value -> BookmarkEntity.Type.PHOTO
            BookmarkEntity.Type.VIDEO.value -> BookmarkEntity.Type.VIDEO
            else -> throw IllegalStateException("$stateValue can not be converted to BookmarkEntity.Type")
        }
    }
}
