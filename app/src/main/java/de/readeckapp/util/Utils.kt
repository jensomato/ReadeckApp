package de.readeckapp.util

import android.content.Context
import android.content.Intent
import java.net.URL
import androidx.core.net.toUri
import androidx.browser.customtabs.CustomTabsIntent
import de.readeckapp.domain.model.SharedText

fun String?.isValidUrl(): Boolean {
    return try {
        URL(this).toURI()
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Detects the first valid URL in a given string and returns it along with the rest of the string as the title.
 * This function handles cases where the URL might be embedded within a larger string.
 *
 * @return A SharedText object containing the URL and title, or null if no valid URL is found.
 */
fun String?.extractUrlAndTitle(): SharedText? {
    if (this.isNullOrBlank()) {
        return null
    }

    val lines = this.lines()
    var foundUrl: String? = null
    val titleBuilder = StringBuilder()
    var urlFound = false

    for (line in lines) {
        val trimmedLine = line.trim()
        val urlInLine = findFirstUrlInLine(trimmedLine)

        if (urlInLine != null) {
            if (!urlFound) {
                foundUrl = urlInLine
                urlFound = true
                // The title is everything before and after the URL on this line, plus subsequent lines
                val urlIndex = trimmedLine.indexOf(urlInLine)
                if (urlIndex > 0) {
                    titleBuilder.append(trimmedLine.substring(0, urlIndex).trim())
                }
                if (titleBuilder.isNotEmpty()) {
                    titleBuilder.append("\n")
                }
                val afterUrl = trimmedLine.substring(urlIndex + urlInLine.length).trim()
                if (afterUrl.isNotEmpty()) {
                    titleBuilder.append(afterUrl)
                    titleBuilder.append("\n")
                }
            } else {
                // If URL already found, append this entire line to the title
                if (titleBuilder.isNotEmpty()) {
                    titleBuilder.append("\n")
                }
                titleBuilder.append(line) // Append original line to preserve formatting if needed
            }
        } else {
            // If URL has not been found yet, this line is part of the potential title
            if (!urlFound) {
                if (titleBuilder.isNotEmpty()) {
                    titleBuilder.append("\n")
                }
                titleBuilder.append(line) // Append original line to preserve formatting if needed
            }
        }
    }

    return if (foundUrl != null && foundUrl.isValidUrl()) {
        SharedText(url = foundUrl, title = titleBuilder.toString().trim().ifBlank { null })
    } else {
        null
    }
}

/**
 * Finds the first valid URL within a given string.
 *
 * @return The first valid URL found, or null if none is present.
 */
private fun findFirstUrlInLine(line: String): String? {
    return URL_REGEX.find(line)?.value
}

private val URL_REGEX = """(https?://[^\s]+)""".toRegex()

fun openUrlInCustomTab(context: Context, url: String) {
    if(url.isValidUrl()) {
        try {
            val builder = CustomTabsIntent.Builder()
            builder.setStartAnimations(context, android.R.anim.fade_in, android.R.anim.fade_out)
            builder.setExitAnimations(context, android.R.anim.fade_in, android.R.anim.fade_out)

            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(context, url.toUri())
        } catch (e: Exception) {
            // Fallback: Open in standard browser if Custom Tabs fails or is not available
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent)
        }
    }
}
