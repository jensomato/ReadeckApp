package de.readeckapp.io.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.readeckapp.io.db.dao.BookmarkDao
import de.readeckapp.io.db.model.ArticleContentEntity
import de.readeckapp.io.db.model.BookmarkEntity
import de.readeckapp.io.db.model.RemoteBookmarkIdEntity

@Database(
    entities = [BookmarkEntity::class, ArticleContentEntity::class, RemoteBookmarkIdEntity::class],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ReadeckDatabase : RoomDatabase() {
    abstract fun getBookmarkDao(): BookmarkDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create the article_content table
                database.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS `article_content` (
                            `bookmarkId` TEXT NOT NULL,
                            `content` TEXT NOT NULL,
                            PRIMARY KEY(`bookmarkId`),
                            FOREIGN KEY(`bookmarkId`) REFERENCES `bookmarks`(`id`) ON DELETE CASCADE
                        )
                    """
                )

                // Copy data from bookmarks.articleContent to article_content.content
                database.execSQL(
                    """
                        INSERT INTO article_content (bookmarkId, content)
                        SELECT id, articleContent FROM bookmarks WHERE articleContent IS NOT NULL
                    """
                )

                // Remove the articleContent column from the bookmarks table
                database.execSQL(
                    """
                        ALTER TABLE bookmarks DROP COLUMN articleContent
                    """
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """                                                                                                                                     
                    CREATE TABLE IF NOT EXISTS `remote_bookmark_ids` ( `id` TEXT NOT NULL, PRIMARY KEY(`id`))                                                            
                """
                )
            }
        }
    }

}
