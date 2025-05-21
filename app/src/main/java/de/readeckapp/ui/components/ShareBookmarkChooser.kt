package de.readeckapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import de.readeckapp.ui.IBookmarkViewModel
import android.content.Intent

@Composable
fun ShareBookmarkChooser(viewModel : IBookmarkViewModel){
    val shareIntent = viewModel.shareIntent.collectAsState().value
    val context = LocalContext.current

    if (shareIntent != null) {
        val chooser = Intent.createChooser(shareIntent, null)
        context.startActivity(chooser)
        viewModel.onShareIntentConsumed()
    }
}