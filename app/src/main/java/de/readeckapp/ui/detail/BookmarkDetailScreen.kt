package de.readeckapp.ui.detail

import android.icu.text.MessageFormat
import android.view.View
import android.webkit.WebView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Share
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
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import de.readeckapp.R
import de.readeckapp.domain.model.Template
import de.readeckapp.ui.components.ErrorPlaceholderImage
import de.readeckapp.util.openUrlInCustomTab
import de.readeckapp.ui.components.ShareBookmarkChooser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun BookmarkDetailScreen(navHostController: NavController, bookmarkId: String?) {
    val viewModel: BookmarkDetailViewModel = hiltViewModel()
    val navigationEvent = viewModel.navigationEvent.collectAsState()
    val openUrlEvent = viewModel.openUrlEvent.collectAsState()
    val onClickBack: () -> Unit = { viewModel.onClickBack() }
    val onClickToggleFavorite: (String, Boolean) -> Unit =
        { id, isFavorite -> viewModel.onToggleFavorite(id, isFavorite) }
    val onClickToggleArchive: (String, Boolean) -> Unit =
        { id, isArchived -> viewModel.onToggleArchive(id, isArchived) }
    val onMarkRead: (String, Boolean) -> Unit =
        { id, isRead -> viewModel.onToggleMarkRead(id, isRead) }

    val onClickOpenUrl: (String) -> Unit = { viewModel.onClickOpenUrl(it) }
    val onClickShareBookmark: (String) -> Unit = { url -> viewModel.onClickShareBookmark(url) }
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

    val context = LocalContext.current
    LaunchedEffect(key1 = openUrlEvent.value){
        openUrlInCustomTab(context, openUrlEvent.value)
        viewModel.onOpenUrlEventConsumed()
    }

    when (uiState) {
        is BookmarkDetailViewModel.UiState.Success -> {
            val successMessage = stringResource(R.string.update_successful)
            LaunchedEffect(key1 = uiState) {
                uiState.updateBookmarkState?.let {
                    when (it) {
                        is BookmarkDetailViewModel.UpdateBookmarkState.Success -> {
                            snackbarHostState.showSnackbar(
                                message = successMessage,
                                duration = SnackbarDuration.Short
                            )
                        }
                        is BookmarkDetailViewModel.UpdateBookmarkState.Error -> {
                            snackbarHostState.showSnackbar(
                                message = it.message,
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
                onClickShareBookmark = onClickShareBookmark,
                onClickDeleteBookmark = onClickDeleteBookmark,
                uiState = uiState,
                onClickOpenUrl = onClickOpenUrl
            )
            // Consumes a shareIntent and creates the corresponding share dialog
            ShareBookmarkChooser(
                context = LocalContext.current,
                intent = viewModel.shareIntent.collectAsState().value,
                onShareIntentConsumed = { viewModel.onShareIntentConsumed() }
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
            BookmarkDetailErrorScreen()
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
    onClickDeleteBookmark: (String) -> Unit,
    onClickOpenUrl: (String) -> Unit,
    onClickShareBookmark: (String) -> Unit,
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
                onClickShareBookmark = onClickShareBookmark,
                onClickDeleteBookmark = onClickDeleteBookmark
            )
        }
    ) { padding ->
        BookmarkDetailContent(
            modifier = Modifier.padding(padding),
            uiState = uiState,
            onClickOpenUrl = onClickOpenUrl
        )
    }
}

@Composable
fun BookmarkDetailContent(
    modifier: Modifier = Modifier,
    uiState: BookmarkDetailViewModel.UiState.Success,
    onClickOpenUrl: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BookmarkDetailHeader(
            modifier = Modifier,
            uiState = uiState,
            onClickOpenUrl = onClickOpenUrl
        )
        if (uiState.bookmark.articleContent != null) {
            BookmarkDetailArticle(modifier = Modifier, uiState = uiState)
        } else {
            EmptyBookmarkDetailArticle(
                modifier = Modifier
            )
        }
    }
}

@Composable
fun EmptyBookmarkDetailArticle(
    modifier: Modifier
) {
    Text(
        modifier = modifier,
        text = stringResource(R.string.detail_view_no_content)
    )
}

@Composable
fun BookmarkDetailArticle(
    modifier: Modifier,
    uiState: BookmarkDetailViewModel.UiState.Success
) {
    val isSystemInDarkMode = isSystemInDarkTheme()
    val content = remember(isSystemInDarkMode, uiState.template) {
        mutableStateOf<String?>(null)
    }
    LaunchedEffect(isSystemInDarkMode, uiState.template) {
        content.value = getTemplate(uiState, isSystemInDarkMode)
    }
    if (content.value != null) {
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
                    it.loadDataWithBaseURL(
                        null,
                        content.value!!,
                        "text/html",
                        "utf-8",
                        null
                    )
                }
            )
        }

    } else {
        CircularProgressIndicator()
    }
}

suspend fun getTemplate(uiState: BookmarkDetailViewModel.UiState.Success, isSystemInDarkMode: Boolean): String? {
    return withContext(Dispatchers.IO) {
        uiState.bookmark.getContent(uiState.template, isSystemInDarkMode)
    }
}


@Composable
fun BookmarkDetailHeader(
    modifier: Modifier,
    uiState: BookmarkDetailViewModel.UiState.Success,
    onClickOpenUrl: (String) -> Unit
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
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(uiState.bookmark.imgSrc)
                .crossfade(true).build(),
            contentDescription = stringResource(R.string.common_bookmark_image_content_description),
            contentScale = ContentScale.FillWidth,
            error = {
                ErrorPlaceholderImage(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    imageContentDescription = stringResource(R.string.common_bookmark_image_content_description)
                )
            },
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.clickable {
                onClickOpenUrl(uiState.bookmark.url)
            }
        ) {
            Text(
                text = uiState.bookmark.siteName,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = stringResource(R.string.action_open_in_browser),
                modifier = Modifier
                    .height(16.dp)
                    .width(16.dp)
            )
        }

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
    onClickShareBookmark: (String) -> Unit,
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
                text = { Text(stringResource(R.string.action_share)) },
                onClick = {
                    onClickShareBookmark(uiState.bookmark.url)
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = stringResource(R.string.action_share)
                    )
                }

            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_delete)) },
                onClick = {
                    onClickDeleteBookmark(uiState.bookmark.bookmarkId)
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.action_delete)
                    )
                }
            )
        }
    }
}

@Composable
fun BookmarkDetailErrorScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(stringResource(R.string.error_no_article_content))
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
        onClickShareBookmark = {_ -> },
        onClickToggleArchive = { _, _ -> },
        uiState = BookmarkDetailViewModel.UiState.Success(
            bookmark = sampleBookmark,
            updateBookmarkState = null,
            template = Template.SimpleTemplate("template")
        ),
        onClickOpenUrl = {}
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
                updateBookmarkState = null,
                template = Template.SimpleTemplate("template")
            ),
            onClickOpenUrl = {}
        )
    }
}

@Preview
@Composable
private fun BookmarkDetailContentErrorPreview() {
    Surface {
        BookmarkDetailErrorScreen()
    }
}

@Preview(showBackground = true)
@Composable
private fun BookmarkDetailHeaderPreview() {
    BookmarkDetailHeader(
        modifier = Modifier,
        uiState = BookmarkDetailViewModel.UiState.Success(
            bookmark = sampleBookmark,
            updateBookmarkState = null,
            template = Template.SimpleTemplate("template")
        ),
        onClickOpenUrl = {}
    )
}


private val sampleBookmark = BookmarkDetailViewModel.Bookmark(
    bookmarkId = "1",
    createdDate = "2024-01-15T10:00:00",
    url = "https://example.com",
    title = "This is a very long title of a small sample bookmark",
    siteName = "Example",
    authors = listOf("John Doe"),
    imgSrc = "https://via.placeholder.com/150",
    isFavorite = false,
    isArchived = false,
    isRead = false,
    type = BookmarkDetailViewModel.Bookmark.Type.ARTICLE,
    articleContent = "articleContent"
)
