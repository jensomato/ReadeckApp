package de.readeckapp.io.rest

import de.readeckapp.io.rest.model.BookmarkDto
import de.readeckapp.io.rest.model.UserProfileDto
import de.readeckapp.io.rest.model.CreateBookmarkDto
import de.readeckapp.io.rest.model.StatusMessageDto
import de.readeckapp.io.rest.model.EditBookmarkDto
import de.readeckapp.io.rest.model.EditBookmarkResponseDto
import de.readeckapp.io.rest.model.SyncInfoDto
import kotlinx.datetime.Instant
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Query
import retrofit2.http.DELETE
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.Path

interface ReadeckApi {
    @GET("/api/bookmarks")
    suspend fun getBookmarks(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("updated_since") updatedSince: Instant?,
        @Query("sort") sortOrder: SortOrder,
        @Query("id") idList: List<String>? = null,
    ): Response<List<BookmarkDto>>

    @GET("/api/profile")
    suspend fun userprofile(): Response<UserProfileDto>

    @Headers("Content-Type: text/html")
    @GET("/api/bookmarks/{id}/article")
    suspend fun getArticle(@Path("id") id: String): Response<String>

    @GET("/api/bookmarks/sync")
    suspend fun getBookmarkSyncList(
        @Query("since") since: Instant
    ): Response<List<SyncInfoDto>>

    @POST("/api/bookmarks")
    suspend fun createBookmark(
        @Body body: CreateBookmarkDto
    ): Response<StatusMessageDto>

    @Headers("Accept: application/json")
    @PATCH("/api/bookmarks/{id}")
    suspend fun editBookmark(
        @Path("id") id: String,
        @Body body: EditBookmarkDto
    ): Response<EditBookmarkResponseDto>

    @Headers("Accept: application/json")
    @DELETE("/api/bookmarks/{id}")
    suspend fun deleteBookmark(@Path("id") id: String): Response<Unit>

    data class SortOrder(val sort: Sort, val order: Order = Order.Ascending) {
        override fun toString(): String {
            return "${order.value}${sort.value}"
        }
    }

    sealed class Sort(val value: String) {
        data object Created: Sort("created")
        data object Title: Sort("title")
        data object Domain: Sort("domain")
        data object Duration: Sort("duration")
        data object Published: Sort("published")
        data object Site: Sort("site")
    }

    sealed class Order(val value: String) {
        data object Ascending: Order("")
        data object Descending: Order("-")
    }

    interface Header {
        companion object {
            const val TOTAL_PAGES = "total-pages"
            const val TOTAL_COUNT = "total-count"
            const val CURRENT_PAGE = "current-page"
            const val BOOKMARK_ID = "bookmark-id"
        }
    }
}
