package de.readeckapp.io.db

import android.content.ContentValues
import android.database.DatabaseUtils
import androidx.room.migration.AutoMigrationSpec
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ReadeckDatabaseMigrationTest {
    private val TEST_DB = "migration-test"

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        val helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            ReadeckDatabase::class.java,
            emptyList<AutoMigrationSpec>(), // workaround for https://issuetracker.google.com/issues/298459978
            FrameworkSQLiteOpenHelperFactory()
        )
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL(
                """
                INSERT INTO bookmarks (
                    id,
                    href,
                    created,
                    updated,
                    state,
                    loaded,
                    url,
                    title,
                    siteName,
                    site,
                    authors,
                    lang,
                    textDirection,
                    documentTpe,
                    type,
                    hasArticle,
                    description,
                    isDeleted,
                    isMarked,
                    isArchived,
                    labels,
                    readProgress,
                    wordCount,
                    readingTime,
                    article_src,
                    icon_src,
                    icon_width,
                    icon_height,
                    image_src,
                    image_width,
                    image_height,
                    log_src,
                    props_src,
                    thumbnail_src,
                    thumbnail_width,
                    thumbnail_height,
                    articleContent
                ) VALUES (
                    'id1',
                    'test-href',
                    1672531200000,
                    1672531200000,
                    0,
                    1,
                    'test-url',
                    'test-title',
                    'test-siteName',
                    'test-site',
                    'test-authors',
                    'test-lang',
                    'test-textDirection',
                    'test-documentTpe',
                    'article',
                    1,
                    'test-description',
                    0,
                    0,
                    0,
                    'test-labels',
                    50,
                    100,
                    5,
                    'test-article_src',
                    'test-icon_src',
                    50,
                    50,
                    'test-image_src',
                    200,
                    100,
                    'test-log_src',
                    'test-props_src',
                    'test-thumbnail_src',
                    100,
                    100,
                    'test-articleContent'
                )
                """
            )
            execSQL(
            """
                INSERT INTO bookmarks (
                    id,
                    href,
                    created,
                    updated,
                    state,
                    loaded,
                    url,
                    title,
                    siteName,
                    site,
                    authors,
                    lang,
                    textDirection,
                    documentTpe,
                    type,
                    hasArticle,
                    description,
                    isDeleted,
                    isMarked,
                    isArchived,
                    labels,
                    readProgress,
                    wordCount,
                    readingTime,
                    article_src,
                    icon_src,
                    icon_width,
                    icon_height,
                    image_src,
                    image_width,
                    image_height,
                    log_src,
                    props_src,
                    thumbnail_src,
                    thumbnail_width,
                    thumbnail_height,
                    articleContent
                ) VALUES (
                    'id2',
                    'test-href',
                    1672531200000,
                    1672531200000,
                    0,
                    1,
                    'test-url',
                    'test-title',
                    'test-siteName',
                    'test-site',
                    'test-authors',
                    'test-lang',
                    'test-textDirection',
                    'test-documentTpe',
                    'video',
                    1,
                    'test-description',
                    0,
                    0,
                    0,
                    'test-labels',
                    50,
                    100,
                    5,
                    'test-article_src',
                    'test-icon_src',
                    50,
                    50,
                    'test-image_src',
                    200,
                    100,
                    'test-log_src',
                    'test-props_src',
                    'test-thumbnail_src',
                    100,
                    100,
                    null
                )
                """
            )
            execSQL(
                """
                INSERT INTO bookmarks (
                    id,
                    href,
                    created,
                    updated,
                    state,
                    loaded,
                    url,
                    title,
                    siteName,
                    site,
                    authors,
                    lang,
                    textDirection,
                    documentTpe,
                    type,
                    hasArticle,
                    description,
                    isDeleted,
                    isMarked,
                    isArchived,
                    labels,
                    readProgress,
                    wordCount,
                    readingTime,
                    article_src,
                    icon_src,
                    icon_width,
                    icon_height,
                    image_src,
                    image_width,
                    image_height,
                    log_src,
                    props_src,
                    thumbnail_src,
                    thumbnail_width,
                    thumbnail_height,
                    articleContent
                ) VALUES (
                    'id3',
                    'test-href',
                    1672531200000,
                    1672531200000,
                    0,
                    1,
                    'test-url',
                    'test-title',
                    'test-siteName',
                    'test-site',
                    'test-authors',
                    'test-lang',
                    'test-textDirection',
                    'test-documentTpe',
                    'photo',
                    1,
                    'test-description',
                    0,
                    0,
                    0,
                    'test-labels',
                    50,
                    100,
                    5,
                    'test-article_src',
                    'test-icon_src',
                    50,
                    50,
                    'test-image_src',
                    200,
                    100,
                    'test-log_src',
                    'test-props_src',
                    'test-thumbnail_src',
                    100,
                    100,
                    null
                )
                """
            )
            close()
        }

        val dbV2 = helper.runMigrationsAndValidate(TEST_DB, 2, true, ReadeckDatabase.MIGRATION_1_2)

        val cursor = dbV2.query("SELECT content FROM article_content WHERE bookmarkId = 'id1'")
        try {
            assertTrue(cursor.moveToFirst())
            val content = cursor.getString(0)
            cursor.close()
            assertTrue(content == "test-articleContent")
        } finally {
            cursor.close()
        }

        var cursor2 = dbV2.query("PRAGMA table_info('bookmarks')")
        try {
            while (cursor2.moveToNext()) {
                var contentValues = ContentValues()
                DatabaseUtils.cursorRowToContentValues(cursor2, contentValues)
                assertNotEquals("articleContent", contentValues.get("name"))
                println("$contentValues")
            }
        } finally {
            cursor2.close()
        }
        dbV2.close()
    }
}
