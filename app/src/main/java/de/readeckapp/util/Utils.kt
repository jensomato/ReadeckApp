package de.readeckapp.util

import java.net.URL

fun String?.isValidUrl(): Boolean {
    return try {
        URL(this).toURI()
        true
    } catch (e: Exception) {
        false
    }
}
