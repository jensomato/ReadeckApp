package de.readeckapp.ui.detail

import android.icu.text.MessageFormat
import android.view.View
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import de.readeckapp.R

@Composable
fun BookmarkDetailScreen(navHostController: NavController, bookmarkId: String?) {
    val viewModel: BookmarkDetailViewModel = hiltViewModel()
    val navigationEvent = viewModel.navigationEvent.collectAsState()
    val onClickBack: () -> Unit = { viewModel.onClickBack() }
    val onClickToggleFavorite: (String, Boolean) -> Unit = { id, isFavorite -> viewModel.onToggleFavorite(id, isFavorite) }
    val onClickToggleArchive: (String, Boolean) -> Unit = { id, isArchived -> viewModel.onToggleArchive(id, isArchived) }
    val onMarkRead: (String, Boolean) -> Unit = { id, isRead -> viewModel.onToggleMarkRead(id, isRead) }
    val onClickDeleteBookmark: (String) -> Unit = { viewModel.deleteBookmark(it) }
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState = viewModel.uiState.collectAsState().value

    LaunchedEffect(key1 = navigationEvent.value) {
        navigationEvent.value?.let { event ->
            when (event) {
                is BookmarkDetailViewModel.NavigationEvent.NavigateBack -> {
                    navHostController.popBackStack()
                }
            }
            viewModel.onNavigationEventConsumed() // Consume the event
        }
    }

    when (uiState) {
        is BookmarkDetailViewModel.UiState.Success -> {
            LaunchedEffect(key1 = uiState) {
                uiState.updateBookmarkState?.let {
                    when (it) {
                        is BookmarkDetailViewModel.UpdateBookmarkState.Success -> {
                            snackbarHostState.showSnackbar(
                                message = "success",
                                duration = SnackbarDuration.Short
                            )
                        }
                        else -> {
                            snackbarHostState.showSnackbar(
                                message = "error",
                                duration = SnackbarDuration.Long
                            )
                        }
                    }
                    viewModel.onUpdateBookmarkStateConsumed()
                }
            }
            BookmarkDetailScreen(
                modifier = Modifier,
                snackbarHostState = snackbarHostState,
                onClickBack = onClickBack,
                onClickToggleFavorite = onClickToggleFavorite,
                onClickToggleArchive = onClickToggleArchive,
                onMarkRead = onMarkRead,
                onClickDeleteBookmark = onClickDeleteBookmark,
                uiState = uiState
            )
        }

        is BookmarkDetailViewModel.UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        else -> {
            Text("error")
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkDetailScreen(
    modifier: Modifier,
    snackbarHostState: SnackbarHostState,
    onClickBack: () -> Unit,
    uiState: BookmarkDetailViewModel.UiState.Success,
    onClickToggleFavorite: (String, Boolean) -> Unit,
    onClickToggleArchive: (String, Boolean) -> Unit,
    onMarkRead: (String, Boolean) -> Unit,
    onClickDeleteBookmark: (String) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.bookmark.title,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClickBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            BookmarkDetailMenu(
                uiState = uiState,
                onClickToggleFavorite = onClickToggleFavorite,
                onClickToggleArchive = onClickToggleArchive,
                onMarkRead = onMarkRead,
                onClickDeleteBookmark = onClickDeleteBookmark
            )
        }
    ) { padding ->
        BookmarkDetailContent(
            modifier = Modifier.padding(padding),
            uiState = uiState
        )
    }
}

@Composable
fun BookmarkDetailContent(
    modifier: Modifier = Modifier,
    uiState: BookmarkDetailViewModel.UiState.Success
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BookmarkDetailHeader(modifier = Modifier, uiState = uiState)
        if (!LocalInspectionMode.current) {
            AndroidView(
                modifier = Modifier.padding(0.dp),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = false
                        settings.useWideViewPort = false
                        settings.loadWithOverviewMode = false
                        setLayerType(View.LAYER_TYPE_HARDWARE, null)
                        settings.defaultTextEncodingName = "utf-8"
                        isVerticalScrollBarEnabled = false
                        isHorizontalScrollBarEnabled = false
                    }

                },
                update = {
                    it.loadDataWithBaseURL(null, uiState.bookmark.htmlContent, "text/html", "utf-8", null)
                }
            )
        }
    }
}

@Composable
fun BookmarkDetailHeader(
    modifier: Modifier,
    uiState: BookmarkDetailViewModel.UiState.Success
) {
    val msg = stringResource(R.string.authors)
    val author = MessageFormat.format(
        msg, mapOf(
            "count" to uiState.bookmark.authors.size,
            "author" to uiState.bookmark.authors.firstOrNull()
        )
    )
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Section Start
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(uiState.bookmark.imgSrc)
                .crossfade(true).build(),
            contentDescription = "Bookmark Image",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            text = uiState.bookmark.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth(),
            text = "$author - ${uiState.bookmark.createdDate}",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            modifier = Modifier
                .fillMaxWidth(),
            text = uiState.bookmark.siteName,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Header Section End
    }
}

@Composable
fun BookmarkDetailMenu(
    uiState: BookmarkDetailViewModel.UiState.Success,
    onClickToggleFavorite: (String, Boolean) -> Unit,
    onClickToggleArchive: (String, Boolean) -> Unit,
    onMarkRead: (String, Boolean) -> Unit,
    onClickDeleteBookmark: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "Actions")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_favorite)) },
                onClick = {
                    onClickToggleFavorite(uiState.bookmark.bookmarkId, !uiState.bookmark.isFavorite)
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (uiState.bookmark.isFavorite) Icons.Filled.Grade else Icons.Outlined.Grade,
                        contentDescription = stringResource(R.string.action_favorite)
                    )
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_archive)) },
                onClick = {
                    onClickToggleArchive(uiState.bookmark.bookmarkId, !uiState.bookmark.isArchived)
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (uiState.bookmark.isArchived) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2,
                        contentDescription = stringResource(R.string.action_archive)
                    )
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_mark_read)) },
                onClick = {
                    onMarkRead(uiState.bookmark.bookmarkId, !uiState.bookmark.isRead)
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (uiState.bookmark.isRead) Icons.Filled.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                        contentDescription = stringResource(R.string.action_mark_read)
                    )
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_delete)) },
                onClick = {
                    onClickDeleteBookmark(uiState.bookmark.bookmarkId)
                    expanded = false
                },
                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete)) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarkDetailScreenPreview() {
    BookmarkDetailScreen(
        modifier = Modifier,
        snackbarHostState = SnackbarHostState(),
        onClickBack = {},
        onClickDeleteBookmark = {},
        onClickToggleFavorite = { _, _ -> },
        onMarkRead = { _, _ -> },
        onClickToggleArchive = { _, _ -> },
        uiState = BookmarkDetailViewModel.UiState.Success(
            bookmark = sampleBookmark,
            updateBookmarkState = null
        )
    )
}

@Preview
@Composable
private fun BookmarkDetailContentPreview() {
    Surface {
        BookmarkDetailContent(
            modifier = Modifier,
            uiState = BookmarkDetailViewModel.UiState.Success(
                bookmark = sampleBookmark,
                updateBookmarkState = null
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BookmarkDetailHeaderPreview() {
    BookmarkDetailHeader(
        modifier = Modifier,
        uiState = BookmarkDetailViewModel.UiState.Success(
            bookmark = sampleBookmark,
            updateBookmarkState = null
        )
    )
}


private val sampleBookmark = BookmarkDetailViewModel.Bookmark(
    bookmarkId = "1",
    createdDate = "2024-01-15T10:00:00",
    title = "This is a very long title of a small sample bookmark",
    siteName = "Example",
    authors = listOf("John Doe"),
    imgSrc = "https://via.placeholder.com/150",
    encodedHtmlContent = "encodedHtmlContent",
    htmlContent = "htmlContent",
    isFavorite = false,
    isArchived = false,
    isRead = false
)
