package de.readeckapp.io

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

interface AssetLoader {
    fun loadAsset(fileName: String): String?
}

class AssetLoaderImpl @Inject constructor(@ApplicationContext private val context: Context) : AssetLoader {
    override fun loadAsset(fileName: String): String? {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            Timber.e(e, "Error loading asset: $fileName")
            null
        }
    }
}
