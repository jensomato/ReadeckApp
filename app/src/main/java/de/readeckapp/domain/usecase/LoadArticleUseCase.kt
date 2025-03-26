package de.readeckapp.domain.usecase

import de.readeckapp.domain.BookmarkRepository
import de.readeckapp.io.rest.ReadeckApi
import timber.log.Timber
import javax.inject.Inject

class LoadArticleUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val readeckApi: ReadeckApi,
) {
    suspend fun execute(bookmarkId: String) {
        val bookmark = bookmarkRepository.getBookmarkById(bookmarkId)
        if (bookmark.hasArticle) {
            val response = readeckApi.getArticle(bookmarkId)
            if (response.isSuccessful) {
                val content = response.body()!!
                val bookmarkToSave = bookmark.copy(articleContent = content)
                bookmarkRepository.insertBookmarks(listOf(bookmarkToSave))
                return
            }
        } else {
            Timber.i("Bookmark has no article [type=${bookmark.type}]")
        }
    }
}