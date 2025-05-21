package de.readeckapp.ui

import android.content.Intent
import kotlinx.coroutines.flow.StateFlow

interface IBookmarkViewModel {
    val shareIntent: StateFlow<Intent?>

    fun onShareIntentConsumed()
}