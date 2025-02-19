package de.readeckapp

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.readeckapp.domain.BookmarkRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(bookmarkRepository: BookmarkRepository): ViewModel() {
}