package de.readeckapp.ui.components

import androidx.compose.runtime.Composable
import android.content.Intent
import android.content.Context

@Composable
fun ShareBookmarkChooser(
    context: Context,
    intent: Intent?,
    onShareIntentConsumed: () -> Unit
){
    if (intent != null) {
        val chooser = Intent.createChooser(intent, null)
        context.startActivity(chooser)
        onShareIntentConsumed.invoke()
    }
}