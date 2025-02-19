package de.readeckapp.domain.usecase

import androidx.work.WorkManager
import de.readeckapp.domain.BookmarkRepository
import de.readeckapp.io.db.dao.BookmarkDao
import de.readeckapp.io.rest.ReadeckApi
import javax.inject.Inject

class LoadBookmarksUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val readeckApi: ReadeckApi,
    private val bookmarkDao: BookmarkDao,
    private val workManager: WorkManager
) {
    suspend fun execute() {
/*        Timber.d("Start")
        val pageSize = 10
        var offset = 0
        val updatedSince: Instant? = bookmarkDao.getLastUpdatedBookmark()?.updated
        val response = readeckApi.getBookmarks(pageSize, offset, updatedSince)


        // use this flow to persist every bookmarkentity into the db with bookmarkdao
        val flow = Pager(
            config = PagingConfig(
                pageSize = 10, // Adjust as needed
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { BookmarkPagingSource(readeckApi, updatedSince) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain().toEntity() }
        }.collect {

        }.flowOn(Dispatchers.IO)

        flow.map {

        }.retry(retries = 3) { ex -> }.map {

        }
        flow.collectLatest { pagingData ->
            pagingData.map { }
            bookmarkDao.insertBookmarks(pagingData.toList())
        }*/
    }

//    suspend fun loadArticle(bookmarkEntity: BookmarkEntity) {
//        if (bookmarkEntity.hasArticle) {
//            val response = readeckApi.getArticle(bookmarkEntity.id)
//            if (response.isSuccessful) {
//                bookmarkEntity.articleContent = response.body()
//            }
//        }
//
//    }
//
//    suspend fun loadBookmarkPage(pageSize: Int, offset: Int, updatedSince: Instant?) {
//        val response = readeckApi.getBookmarks(pageSize, offset, updatedSince)
//        if (response.isSuccessful) {
//            val bookmarks = response.body()!!
//            bookmarks.map { it.toDomain() }.map { it.toEntity() }
//                .map {
//                    if (it.hasArticle) {
//                        val r = readeckApi.getArticle(it.id)
//                        if (r.isSuccessful) {
//                            val content = r.body()!!
//                            val bookmark = it.copy(articleContent = content)
//                            bookmarkRepository.insertBookmarks(listOf(bookmark))
//                        }
//                    }
//                }
//        }
//    }
}

//class BookmarkPagingSource(
//    private val readeckApi: ReadeckApi,
//    private val updatedSince: Instant?
//) : PagingSource<Int, BookmarkDto>() {
//    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BookmarkDto> {
//        return try {
//            val offset = params.key ?: 0
//            val limit = params.loadSize
//            val response = readeckApi.getBookmarks(limit, offset, updatedSince)
//            if (response.isSuccessful) {
//                val bookmarkDtos = response.body() ?: emptyList()
//                val nextKey = if (bookmarkDtos.size < limit) null else offset + limit
//                LoadResult.Page(
//                    data = bookmarkDtos,
//                    prevKey = if (offset == 0) null else offset - limit,
//                    nextKey = nextKey
//                )
//            } else {
//                LoadResult.Error(Exception("API error: ${response.code()}"))
//            }
//        } catch (e: Exception) {
//            LoadResult.Error(e)
//        }
//    }
//
//    override fun getRefreshKey(state: PagingState<Int, BookmarkDto>): Int? {
//        return state.anchorPosition?.let { anchorPosition ->
//            val anchorPage = state.closestPageToPosition(anchorPosition)
//            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
//        }
//    }
//}
