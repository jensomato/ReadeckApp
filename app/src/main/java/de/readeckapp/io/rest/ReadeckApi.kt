package de.readeckapp.io.rest

import de.readeckapp.io.rest.model.AuthenticationRequestDto
import de.readeckapp.io.rest.model.AuthenticationResponseDto
import de.readeckapp.io.rest.model.BookmarkDto
import de.readeckapp.io.rest.model.UserProfileDto
import kotlinx.datetime.Instant
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.Path

interface ReadeckApi {
    @GET("bookmarks")
    suspend fun getBookmarks(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("updated_since") updatedSince: Instant?,
        @Query("sort") sortOrder: SortOrder
    ): Response<List<BookmarkDto>>

    @POST("auth")
    suspend fun authenticate(
        @Body body: AuthenticationRequestDto
    ): Response<AuthenticationResponseDto>

    @GET("profile")
    suspend fun userprofile(): Response<UserProfileDto>

    @Headers("Content-Type: text/html")
    @GET("bookmarks/{id}/article")
    suspend fun getArticle(@Path("id") id: String): Response<String>

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
}
