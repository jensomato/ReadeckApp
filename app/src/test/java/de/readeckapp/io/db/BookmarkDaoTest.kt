package de.readeckapp.io.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.readeckapp.io.db.dao.BookmarkDao
import de.readeckapp.io.db.model.ArticleContentEntity
import de.readeckapp.io.db.model.BookmarkEntity
import de.readeckapp.io.db.model.BookmarkWithArticleContent
import de.readeckapp.io.db.model.ImageResourceEntity
import de.readeckapp.io.db.model.RemoteBookmarkIdEntity
import de.readeckapp.io.db.model.ResourceEntity
import de.readeckapp.test.logging.replaceDebugTree
import de.readeckapp.test.logging.restoreDebugTree
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RobolectricTestRunner
import timber.log.Timber

@RunWith(Enclosed::class)
class BookmarkDaoTest {
    internal abstract class BaseTest {
        lateinit var bookmarkDao: BookmarkDao
        private lateinit var db: ReadeckDatabase
        val testDispatcher = StandardTestDispatcher()

        @Before
        fun setup() {
            replaceDebugTree()
            val context: Context = ApplicationProvider.getApplicationContext()
            db = Room.inMemoryDatabaseBuilder(context, ReadeckDatabase::class.java)
                .allowMainThreadQueries().build()
            bookmarkDao = db.getBookmarkDao()
            generateTestData()
        }

        @After
         fun tearDown() {
            db.close()
            restoreDebugTree()
        }

        private fun generateTestData() = runTest {
            val startDate = LocalDate(2025, 1, 1)
            val bookmarkEntities = (0 until 30).map { index ->
                val currentDate = startDate.plus(index.toLong(), kotlinx.datetime.DateTimeUnit.DAY)
                val type = when (index) {
                    in 0..9 -> BookmarkEntity.Type.ARTICLE
                    in 10..19 -> BookmarkEntity.Type.VIDEO
                    in 20..29 -> BookmarkEntity.Type.PHOTO
                    else -> {
                        BookmarkEntity.Type.ARTICLE
                    }
                }
                val state = when (index) {
                    9, 19, 29 -> BookmarkEntity.State.ERROR
                    8, 18, 28 -> BookmarkEntity.State.LOADING
                    else -> BookmarkEntity.State.LOADED
                }
                BookmarkEntity(
                    id = "test-$index",
                    href = "http://example.com/$index",
                    created = currentDate.atStartOfDayIn(TimeZone.UTC),
                    updated = currentDate.atStartOfDayIn(TimeZone.UTC),
                    state = state,
                    loaded = true,
                    url = "http://example.com/$index",
                    title = "Test Bookmark $index",
                    siteName = "Example",
                    site = "example.com",
                    authors = listOf("Author $index"),
                    lang = "en",
                    textDirection = "ltr",
                    documentTpe = type.value.lowercase(),
                    type = type,
                    hasArticle = true,
                    description = "Description for bookmark $index",
                    isDeleted = false,
                    isMarked = false,
                    isArchived = false,
                    labels = listOf("label1", "label2"),
                    readProgress = index * 3,
                    wordCount = 100 + index * 10,
                    readingTime = 5 + index,
                    article = ResourceEntity(""),
                    icon = ImageResourceEntity("", 50, 50),
                    image = ImageResourceEntity("", 200, 100),
                    log = ResourceEntity(""),
                    props = ResourceEntity(""),
                    thumbnail = ImageResourceEntity("", 100, 100),
                )
            }
            val bookmarkArticles = bookmarkEntities.map {
                BookmarkWithArticleContent(
                    bookmark = it,
                    articleContent = if (it.type == BookmarkEntity.Type.ARTICLE) {
                        ArticleContentEntity(bookmarkId = it.id, content = "content")
                    } else {
                        null
                    }
                )
            }
            bookmarkDao.insertBookmarksWithArticleContent(bookmarkArticles)
            val ids = bookmarkArticles.map { RemoteBookmarkIdEntity(it.bookmark.id) }
                .filterNot { it.id == "test-1" || it.id == "test-11" || it.id == "test-21" }
           bookmarkDao.insertRemoteBookmarkIds(ids + RemoteBookmarkIdEntity("not-a-bookmark"))
        }
    }

    @RunWith(ParameterizedRobolectricTestRunner::class)
    internal class GetBookmarkListItemsByFiltersTest(private val parameter: ParameterType) :
        BaseTest() {

        companion object {
            @JvmStatic
            @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
            fun data(): List<ParameterType> = listOf(
                ParameterType(BookmarkEntity.Type.ARTICLE),
                ParameterType(BookmarkEntity.Type.PHOTO),
                ParameterType(BookmarkEntity.Type.VIDEO),
            )
        }

        data class ParameterType(val type: BookmarkEntity.Type)

        @Test
        fun testFilterArticles() = runTest(testDispatcher) {
            val flow = bookmarkDao.getBookmarkListItemsByFilters(parameter.type)
            val list = flow.first()
            assertEquals(10, list.size)
            list.forEach {
                assertEquals(parameter.type, it.type)
            }
        }

    }

    @RunWith(ParameterizedRobolectricTestRunner::class)
    internal class GetBookmarksByFiltersTest(private val parameter: ParameterType) : BaseTest() {

        companion object {
            @JvmStatic
            @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
            fun data(): List<ParameterType> = listOf(
                ParameterType(BookmarkEntity.Type.ARTICLE),
                ParameterType(BookmarkEntity.Type.PHOTO),
                ParameterType(BookmarkEntity.Type.VIDEO),
            )
        }

        data class ParameterType(val type: BookmarkEntity.Type)

        @Test
        fun testFilterArticles() = runTest(testDispatcher) {
            val flow = bookmarkDao.getBookmarksByFilters(parameter.type)
            val list = flow.first()
            assertEquals(10, list.size)
            list.forEach {
                assertEquals(parameter.type, it.type)
            }
        }

    }

    @RunWith(RobolectricTestRunner::class)
    internal class GetLastUpdatedBookmarkTest : BaseTest() {
        @Test
        fun testGetLastUpdatedBookmark() = runTest(testDispatcher) {
            val lastUpdated = bookmarkDao.getLastUpdatedBookmark()
            assertNotNull(lastUpdated)
            assertEquals("test-29", lastUpdated?.id)
        }

    }

    @RunWith(RobolectricTestRunner::class)
    internal class GetAllBookmarksIsSortedByCreationDateTest : BaseTest() {
        @Test
        fun testGetAllBookmarksIsSortedByCreationDate() = runTest(testDispatcher) {
            val flow = bookmarkDao.getAllBookmarks()
            val list = flow.first()
            assertEquals(30, list.size)
            var prevDate: Instant? = null
            list.forEach { bookmark ->
                prevDate?.let {
                    assertTrue("wrong sort order", it >= bookmark.created)
                }
                prevDate = bookmark.created
            }
        }

    }

    @RunWith(RobolectricTestRunner::class)
    internal class GetFilterByStateTest : BaseTest() {
        @Test
        fun testGetLoaded() = runTest(testDispatcher) {
            val flow = bookmarkDao.getBookmarksByFilters(state = BookmarkEntity.State.LOADED)
            val list = flow.first()
            assertEquals(24, list.size)
            list.forEach { bookmark ->
                assertEquals(BookmarkEntity.State.LOADED, bookmark.state)
            }
        }

        @Test
        fun testGetLoading() = runTest(testDispatcher) {
            val flow = bookmarkDao.getBookmarksByFilters(state = BookmarkEntity.State.LOADING)
            val list = flow.first()
            assertEquals(3, list.size)
            list.forEach { bookmark ->
                assertEquals(BookmarkEntity.State.LOADING, bookmark.state)
            }
        }

        @Test
        fun testGetError() = runTest(testDispatcher) {
            val flow = bookmarkDao.getBookmarksByFilters(state = BookmarkEntity.State.ERROR)
            val list = flow.first()
            assertEquals(3, list.size)
            list.forEach { bookmark ->
                assertEquals(BookmarkEntity.State.ERROR, bookmark.state)
            }
        }
    }

    @RunWith(RobolectricTestRunner::class)
    internal class GetArticleTest : BaseTest() {
        @Test
        fun testGetLoaded() = runTest(testDispatcher) {
            val list = bookmarkDao.getAllBookmarksWithContent()
            Timber.d("list=$list")
        }
    }

    @RunWith(RobolectricTestRunner::class)
    internal class RemoteBookmarkIdTest : BaseTest() {
        @Test
        fun testGetRemoteBookmarkIds() = runTest(testDispatcher) {
            val list = bookmarkDao.getAllRemoteBookmarkIds()
            assertEquals(28, list.size)
            list.forEach {
                val bookmark = bookmarkDao.getBookmarkById(it)
                if (bookmark != null) {
                    assertEquals(it, bookmark.id)
                    Timber.d("id=$it")
                    Timber.d("bookmark=$bookmark")
                } else {
                    assertEquals(it, "not-a-bookmark")
                    Timber.d("not-a-bookmark")
                }
            }
        }

        @Test
        fun testRemoveBookmarks() = runTest(testDispatcher) {
            val removedIds = listOf<String>("test-1", "test-11", "test-21")
            removedIds.forEach {
                assertNotNull(bookmarkDao.getBookmarkById(it))
                Timber.d("id=$it is not null")
            }
            val count = bookmarkDao.removeDeletedBookmars()
            assertEquals(3, count)
            removedIds.forEach {
                assertNull(bookmarkDao.getBookmarkById(it))
                Timber.d("id=$it is null")
            }
        }
    }

    @RunWith(RobolectricTestRunner::class)
    internal class GetCountsTest : BaseTest() {
        @Test
        fun testObserveAllBookmarkCounts() = runTest(testDispatcher) {
            val counts = bookmarkDao.observeAllBookmarkCounts().first()
            assertEquals(24, counts?.unread)
            assertEquals(0, counts?.archived)
            assertEquals(0, counts?.favorite)
            assertEquals(8, counts?.article)
            assertEquals(8, counts?.video)
            assertEquals(8, counts?.picture)
            assertEquals(24, counts?.total)
        }
    }
}
