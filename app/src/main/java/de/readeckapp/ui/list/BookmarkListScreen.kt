package de.readeckapp.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import de.readeckapp.R
import de.readeckapp.domain.model.Bookmark
import de.readeckapp.domain.model.BookmarkListItem
import de.readeckapp.ui.components.ShareBookmarkChooser
import de.readeckapp.ui.navigation.BookmarkDetailRoute
import de.readeckapp.ui.navigation.SettingsRoute
import de.readeckapp.util.openUrlInCustomTab
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkListScreen(navHostController: NavHostController) {
    val viewModel: BookmarkListViewModel = hiltViewModel()
    val navigationEvent = viewModel.navigationEvent.collectAsState()
    val openUrlEvent = viewModel.openUrlEvent.collectAsState()
    val uiState = viewModel.uiState.collectAsState().value
    val createBookmarkUiState = viewModel.createBookmarkUiState.collectAsState().value

    // Collect filter states
    val filterState = viewModel.filterState.collectAsState()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val pullToRefreshState = rememberPullToRefreshState()
    val isLoading by viewModel.loadBookmarksIsRunning.collectAsState()

    // UI event handlers (pass filter update functions)
    val onClickAll = { viewModel.onClickAll() }
    val onClickFilterUnread: () -> Unit = { viewModel.onClickUnread() }
    val onClickFilterArchive: () -> Unit = { viewModel.onClickArchive() }
    val onClickFilterFavorite: () -> Unit = { viewModel.onClickFavorite() }
    val onClickFilterArticles: () -> Unit = { viewModel.onClickArticles() }
    val onClickFilterPictures: () -> Unit = { viewModel.onClickPictures() }
    val onClickFilterVideos: () -> Unit = { viewModel.onClickVideos() }
    val onClickSettings: () -> Unit = { viewModel.onClickSettings() }
    val onClickBookmark: (String) -> Unit = { bookmarkId -> viewModel.onClickBookmark(bookmarkId) }
    val onClickDelete: (String) -> Unit = { bookmarkId -> viewModel.onDeleteBookmark(bookmarkId) }
    val onClickMarkRead: (String, Boolean) -> Unit = { bookmarkId, isRead -> viewModel.onToggleMarkReadBookmark(bookmarkId, isRead) }
    val onClickFavorite: (String, Boolean) -> Unit = { bookmarkId, isFavorite -> viewModel.onToggleFavoriteBookmark(bookmarkId, isFavorite) }
    val onClickArchive: (String, Boolean) -> Unit = { bookmarkId, isArchived -> viewModel.onToggleArchiveBookmark(bookmarkId, isArchived) }
    val onClickOpenInBrowser: (String) -> Unit = { url -> viewModel.onClickOpenInBrowser(url) }
    val onClickShareBookmark: (String) -> Unit = { url -> viewModel.onClickShareBookmark(url) }

    LaunchedEffect(key1 = navigationEvent.value) {
        navigationEvent.value?.let { event ->
            when (event) {
                is BookmarkListViewModel.NavigationEvent.NavigateToSettings -> {
                    navHostController.navigate(SettingsRoute)
                    scope.launch { drawerState.close() }
                }

                is BookmarkListViewModel.NavigationEvent.NavigateToBookmarkDetail -> {
                    navHostController.navigate(BookmarkDetailRoute(event.bookmarkId))
                }
            }
            viewModel.onNavigationEventConsumed() // Consume the event
        }
    }

    val context = LocalContext.current
    LaunchedEffect(key1 = openUrlEvent.value) {
        openUrlInCustomTab(context, openUrlEvent.value)
        viewModel.onOpenUrlEventConsumed()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(id = R.string.app_name),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text(text = stringResource(id = R.string.all)) },
                        icon = { Icon(Icons.Outlined.Bookmarks, contentDescription = null) },
                        selected = filterState.value == BookmarkListViewModel.FilterState(),
                        onClick = {
                            onClickAll()
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text(text = stringResource(id = R.string.unread)) },
                        icon = {Icon(Icons.Outlined.TaskAlt, contentDescription = null)},
                        selected = filterState.value.unread == true,
                        onClick = {
                            onClickFilterUnread()
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text(text = stringResource(id = R.string.archive)) },
                        icon = { Icon(Icons.Outlined.Inventory2, contentDescription = null) },
                        selected = filterState.value.archived == true,
                        onClick = {
                            onClickFilterArchive()
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text(text = stringResource(id = R.string.favorites)) },
                        icon = { Icon(Icons.Outlined.Favorite, contentDescription = null) },
                        selected = filterState.value.favorite == true,
                        onClick = {
                            onClickFilterFavorite()
                            scope.launch { drawerState.close() }
                        }
                    )
                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text(text = stringResource(id = R.string.articles)) },
                        icon = { Icon(Icons.Outlined.Description, contentDescription = null) },
                        selected = filterState.value.type == Bookmark.Type.Article,
                        onClick = {
                            onClickFilterArticles()
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text(text = stringResource(id = R.string.videos)) },
                        icon = { Icon(Icons.Outlined.Movie, contentDescription = null) },
                        selected = filterState.value.type == Bookmark.Type.Video,
                        onClick = {
                            onClickFilterVideos()
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text(text = stringResource(id = R.string.pictures)) },
                        icon = { Icon(Icons.Outlined.Image, contentDescription = null) },
                        selected = filterState.value.type == Bookmark.Type.Picture,
                        onClick = {
                            onClickFilterPictures()
                            scope.launch { drawerState.close() }
                        }
                    )
                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text(text = stringResource(id = R.string.settings)) },
                        icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                        selected = false,
                        onClick = {
                            onClickSettings()
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.bookmarks)) },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } }
                        ) {
                            Icon(
                                Icons.Filled.Menu,
                                contentDescription = stringResource(id = R.string.menu)
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { viewModel.openCreateBookmarkDialog() }) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(id = R.string.add_bookmark)
                    )
                }
            }
        ) { padding ->
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.onPullToRefresh() },
                state = pullToRefreshState,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxWidth()
            ) {
                when (uiState) {
                    is BookmarkListViewModel.UiState.Empty -> {
                        EmptyScreen(messageResource = uiState.messageResource)
                    }
                    is BookmarkListViewModel.UiState.Success -> {
                        LaunchedEffect(key1 = uiState.updateBookmarkState) {
                            uiState.updateBookmarkState?.let { result ->
                                val message = when (result) {
                                    is BookmarkListViewModel.UpdateBookmarkState.Success -> {
                                        "success"
                                    }

                                    is BookmarkListViewModel.UpdateBookmarkState.Error -> {
                                        result.message
                                    }
                                }
                                snackbarHostState.showSnackbar(
                                    message = message,
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                        BookmarkListView(
                            bookmarks = uiState.bookmarks,
                            onClickBookmark = onClickBookmark,
                            onClickDelete = onClickDelete,
                            onClickArchive = onClickArchive,
                            onClickFavorite = onClickFavorite,
                            onClickMarkRead = onClickMarkRead,
                            onClickOpenInBrowser = onClickOpenInBrowser,
                            onClickShareBookmark = onClickShareBookmark
                        )
                        // Consumes a shareIntent and creates the corresponding share dialog
                        ShareBookmarkChooser(
                            context = LocalContext.current,
                            intent = viewModel.shareIntent.collectAsState().value,
                            onShareIntentConsumed = { viewModel.onShareIntentConsumed() }
                        )
                    }
                }
            }

            // Show the CreateBookmarkDialog based on the state
            when (createBookmarkUiState) {
                is BookmarkListViewModel.CreateBookmarkUiState.Open -> {
                    CreateBookmarkDialog(
                        onDismiss = { viewModel.closeCreateBookmarkDialog() },
                        title = createBookmarkUiState.title,
                        url = createBookmarkUiState.url,
                        urlError = createBookmarkUiState.urlError,
                        isCreateEnabled = createBookmarkUiState.isCreateEnabled,
                        onTitleChange = { viewModel.updateCreateBookmarkTitle(it) },
                        onUrlChange = { viewModel.updateCreateBookmarkUrl(it) },
                        onCreateBookmark = { viewModel.createBookmark() }
                    )
                }

                is BookmarkListViewModel.CreateBookmarkUiState.Loading -> {
                    // Show a loading indicator
                    Dialog(onDismissRequest = { viewModel.closeCreateBookmarkDialog() }) {
                        CircularProgressIndicator()
                    }
                }

                is BookmarkListViewModel.CreateBookmarkUiState.Success -> {
                    // Optionally show a success message
                    LaunchedEffect(key1 = createBookmarkUiState) {
                        // Dismiss the dialog after a short delay
                        scope.launch {
                            kotlinx.coroutines.delay(1000)
                            viewModel.closeCreateBookmarkDialog()
                        }
                    }
                }

                is BookmarkListViewModel.CreateBookmarkUiState.Error -> {
                    // Show an error message
                    AlertDialog(
                        onDismissRequest = { viewModel.closeCreateBookmarkDialog() },
                        title = { Text(stringResource(id = R.string.error)) },
                        text = { Text(createBookmarkUiState.message) },
                        confirmButton = {
                            TextButton(onClick = { viewModel.closeCreateBookmarkDialog() }) {
                                Text(stringResource(id = R.string.ok))
                            }
                        }
                    )
                }

                is BookmarkListViewModel.CreateBookmarkUiState.Closed -> {
                    // Do nothing when the dialog is closed
                }
            }
        }
    }
}

@Composable
fun CreateBookmarkDialog(
    onDismiss: () -> Unit,
    title: String,
    url: String,
    urlError: Int?,
    isCreateEnabled: Boolean,
    onTitleChange: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onCreateBookmark: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.add_new_bookmark)) },
        text = {
            Column {
                OutlinedTextField(
                    value = url,
                    onValueChange = { onUrlChange(it) },
                    isError = urlError != null,
                    label = { Text(stringResource(id = R.string.url)) },
                    supportingText = {
                        urlError?.let {
                            Text(text = stringResource(it))
                        }
                    }
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { onTitleChange(it) },
                    label = { Text(stringResource(id = R.string.title)) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreateBookmark()
                },
                enabled = isCreateEnabled
            ) {
                Text(stringResource(id = R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}

/*@Composable
fun ErrorScreen(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(stringResource(id = R.string.an_error_occurred))
        }
    }
}*/

@Composable
fun EmptyScreen(
    modifier: Modifier = Modifier,
    messageResource: Int
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(stringResource(id = messageResource))
        }
    }
}

@Composable
fun BookmarkListView(
    modifier: Modifier = Modifier,
    bookmarks: List<BookmarkListItem>,
    onClickBookmark: (String) -> Unit,
    onClickDelete: (String) -> Unit,
    onClickMarkRead: (String, Boolean) -> Unit,
    onClickFavorite: (String, Boolean) -> Unit,
    onClickArchive: (String, Boolean) -> Unit,
    onClickOpenInBrowser: (String) -> Unit,
    onClickShareBookmark: (String) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(bookmarks) { bookmark ->
            BookmarkCard(
                bookmark = bookmark,
                onClickCard = onClickBookmark,
                onClickDelete = onClickDelete,
                onClickArchive = onClickArchive,
                onClickFavorite = onClickFavorite,
                onClickMarkRead = onClickMarkRead,
                onClickOpenUrl = onClickOpenInBrowser,
                onClickShareBookmark = onClickShareBookmark
            )
        }
    }
}

/*@Preview
@Composable
fun ErrorScreenPreview() {
    ErrorScreen()
}*/

@Preview
@Composable
fun EmptyScreenPreview() {
    EmptyScreen(messageResource = R.string.list_view_empty_nothing_to_see)
}

@Preview(showBackground = true)
@Composable
fun BookmarkListViewPreview() {
    val sampleBookmark = BookmarkListItem(
        id = "1",
        url = "https://example.com",
        title = "Sample Bookmark",
        siteName = "Example",
        type = Bookmark.Type.Article,
        isMarked = false,
        isArchived = false,
        labels = listOf(
            "one",
            "two",
            "three",
            "fourhundretandtwentyone",
            "threethousendtwohundretandfive"
        ),
        isRead = true,
        iconSrc = "https://picsum.photos/seed/picsum/640/480",
        imageSrc = "https://picsum.photos/seed/picsum/640/480",
        thumbnailSrc = "https://picsum.photos/seed/picsum/640/480",
    )
    val bookmarks = listOf(sampleBookmark)

    // Provide a dummy NavHostController for the preview
    val navController = rememberNavController()
    BookmarkListView(
        modifier = Modifier,
        bookmarks = bookmarks,
        onClickBookmark = {},
        onClickDelete = {},
        onClickArchive = { _, _ -> },
        onClickFavorite = { _, _ -> },
        onClickMarkRead = { _, _ -> },
        onClickOpenInBrowser = {},
        onClickShareBookmark = {_ -> }
    )
}
