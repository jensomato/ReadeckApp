package de.readeckapp.io.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import de.readeckapp.io.db.model.BookmarkEntity
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

@Dao
interface BookmarkDao {
    @Query("SELECT * from bookmarks")
    suspend fun getBookmarks(): List<BookmarkEntity>

    @Query("SELECT * from bookmarks")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * from bookmarks WHERE type = 'picture'")
    fun getPictures(): Flow<List<BookmarkEntity>>

    @Query("SELECT * from bookmarks WHERE type = 'video'")
    fun getVideos(): Flow<List<BookmarkEntity>>

    @Query("SELECT * from bookmarks WHERE type = 'article'")
    fun getArticles(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmarks(bookmarks: List<BookmarkEntity>)

    @Query("SELECT * FROM bookmarks ORDER BY updated DESC LIMIT 1")
    suspend fun getLastUpdatedBookmark(): BookmarkEntity?

    @Query("SELECT * FROM bookmarks WHERE id = :id")
    suspend fun getBookmarkById(id: String): BookmarkEntity

    @RawQuery(observedEntities = [BookmarkEntity::class])
    fun getBookmarksByFiltersDynamic(query: SupportSQLiteQuery): Flow<List<BookmarkEntity>>

    fun getBookmarksByFilters(
        type: BookmarkEntity.Type? = null,
        isUnread: Boolean? = null,
        isArchived: Boolean? = null,
        isFavorite: Boolean? = null
    ): Flow<List<BookmarkEntity>> {
        val args = mutableListOf<Any>()
        val sqlQuery = buildString {
            append("SELECT * FROM bookmarks WHERE 1=1")

            type?.let {
                append(" AND type = ?")
                args.add(it.value)
            }

            if (isUnread == true) {
                append(" AND readProgress < 100")
            } else if (isUnread == false) {
                append(" AND readProgress = 100")
            }

            isArchived?.let {
                append(" AND isArchived = ?")
                args.add(it)
            }

            isFavorite?.let {
                append(" AND isMarked = ?")
                args.add(it)
            }
        }.let { SimpleSQLiteQuery(it, args.toTypedArray()) }
        System.out.println("query=${sqlQuery.sql}")
        Timber.d("query=$sqlQuery")
        return getBookmarksByFiltersDynamic(sqlQuery)
    }
}
