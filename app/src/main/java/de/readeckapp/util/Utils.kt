package de.readeckapp.util

import android.content.Context
import android.content.Intent
import java.net.URL
import androidx.core.net.toUri
import androidx.browser.customtabs.CustomTabsIntent

fun String?.isValidUrl(): Boolean {
    return try {
        URL(this).toURI()
        true
    } catch (e: Exception) {
        false
    }
}

fun openUrlInCustomTab(context: Context, url: String) {
    if(url.isValidUrl()){
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
