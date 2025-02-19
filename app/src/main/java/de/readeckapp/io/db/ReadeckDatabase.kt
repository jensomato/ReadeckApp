package de.readeckapp.io.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.readeckapp.io.db.dao.BookmarkDao
import de.readeckapp.io.db.model.BookmarkEntity

@Database(
    entities = [BookmarkEntity::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class ReadeckDatabase : RoomDatabase() {
    abstract fun getBookmarkDao(): BookmarkDao
}
