package de.readeckapp.util

import de.readeckapp.domain.model.SharedText
import junit.framework.TestCase.assertEquals
import org.junit.Test

class UtilsTest {
    @Test
    fun testExtractUrlAndTitle() {
        val testSet = listOf<Pair<String, SharedText?>>(
            "test " to null,
            "https://example.com" to SharedText(url = "https://example.com"),
            "before title https://example.com" to SharedText(url = "https://example.com", "before title"),
            "https://example.com after title" to SharedText(url = "https://example.com", "after title"),
        )
        testSet.forEachIndexed { index, testSet ->
            assertEquals("Error in testSet $index", testSet.second, testSet.first.extractUrlAndTitle())
        }
    }

}