package de.readeckapp.io.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.readeckapp.io.db.dao.BookmarkDao
import de.readeckapp.io.db.model.BookmarkEntity
import de.readeckapp.io.db.model.ImageResourceEntity
import de.readeckapp.io.db.model.ResourceEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RobolectricTestRunner

@RunWith(Enclosed::class)
class BookmarkDaoTest {
    internal abstract class BaseTest() {
        lateinit var bookmarkDao: BookmarkDao
        private lateinit var db: ReadeckDatabase
        val testDispatcher = StandardTestDispatcher()

        @Before
        fun setup() {
            val context: Context = ApplicationProvider.getApplicationContext()
            db = Room.inMemoryDatabaseBuilder(context, ReadeckDatabase::class.java)
                .allowMainThreadQueries().build()
            bookmarkDao = db.getBookmarkDao()
            generateTestData()
        }

        @After
        fun tearDown() {
            db.close()
        }

        fun generateTestData() = runTest {
            val startDate = LocalDate(2025, 1, 1)
            val bookmarkEntities = (0 until 30).map { index ->
                val currentDate = startDate.plus(index.toLong(), kotlinx.datetime.DateTimeUnit.DAY)
                val type = when (index) {
                    in 0..9 -> BookmarkEntity.Type.Article.value
                    in 10..19 -> BookmarkEntity.Type.Video.value
                    in 20..29 -> BookmarkEntity.Type.Picture.value
                    else -> {
                        BookmarkEntity.Type.Article.value
                    }
                }
                BookmarkEntity(
                    id = "test-$index",
                    href = "http://example.com/$index",
                    created = currentDate.atStartOfDayIn(TimeZone.UTC),
                    updated = currentDate.atStartOfDayIn(TimeZone.UTC),
                    state = 0,
                    loaded = true,
                    url = "http://example.com/$index",
                    title = "Test Bookmark $index",
                    siteName = "Example",
                    site = "example.com",
                    authors = listOf("Author $index"),
                    lang = "en",
                    textDirection = "ltr",
                    documentTpe = type,
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
                    articleContent = ""
                )
            }
            bookmarkDao.insertBookmarks(bookmarkEntities)
        }
    }

    @RunWith(ParameterizedRobolectricTestRunner::class)
    internal class GetBookmarksByFiltersTest(private val parameter: ParameterType) : BaseTest() {

        companion object {
            @JvmStatic
            @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
            fun data(): List<ParameterType> = listOf(
                ParameterType(BookmarkEntity.Type.Article),
                ParameterType(BookmarkEntity.Type.Picture),
                ParameterType(BookmarkEntity.Type.Video),
            )
        }

        data class ParameterType(val type: BookmarkEntity.Type)

        @Test
        fun testFilterArticles() = runTest(testDispatcher) {
            val flow = bookmarkDao.getBookmarksByFilters(parameter.type)
            val list = flow.first()
            assertEquals(10, list.size)
            list.forEach {
                assertEquals(parameter.type.value, it.type)
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
}
