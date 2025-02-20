package de.readeckapp.ui.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import de.readeckapp.domain.model.Bookmark
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkListScreen(navHostController: NavHostController) {
    val viewModel: BookmarkListViewModel = hiltViewModel()
    val navigationEvent = viewModel.navigationEvent.collectAsState()
    val bookmarks = viewModel.bookmarks.value
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val onClickAll = { viewModel.onClickAll() }
    val onClickFilterUnread: () -> Unit = { viewModel.onClickUnread() }
    val onClickFilterArchive: () -> Unit = { viewModel.onClickArchive() }
    val onClickFilterFavorite: () -> Unit = { viewModel.onClickFavorite() }
    val onClickFilterArticles: () -> Unit = { viewModel.onClickArticles() }
    val onClickFilterPictures: () -> Unit = { viewModel.onClickPictures() }
    val onClickFilterVideos: () -> Unit = { viewModel.onClickVideos() }
    val onClickFilterCollections: () -> Unit = { viewModel.onClickCollections() }
    val onClickFilterLabels: () -> Unit = { viewModel.onClickLabels() }
    val onClickSettings: () -> Unit = { viewModel.onClickSettings() }
    val onClickBookmark: (String) -> Unit = { bookmarkId -> viewModel.onClickBookmark(bookmarkId) }
    val onClickDelete: (String) -> Unit = { bookmarkId -> viewModel.onDeleteBookmark(bookmarkId) }
    val onClickMarkRead: (String) -> Unit = { bookmarkId -> viewModel.onToggleMarkReadBookmark(bookmarkId) }
    val onClickFavorite: (String) -> Unit = { bookmarkId -> viewModel.onToggleFavoriteBookmark(bookmarkId) }
    val onClickArchive: (String) -> Unit = { bookmarkId -> viewModel.onToggleArchiveBookmark(bookmarkId) }
    LaunchedEffect(key1 = navigationEvent.value) {
        navigationEvent.value?.let { event ->
            when (event) {
                is BookmarkListViewModel.NavigationEvent.NavigateToSettings -> {
                    navHostController.navigate("settings")
                    drawerState.close()
                }
                is BookmarkListViewModel.NavigationEvent.NavigateToBookmarkDetail -> {
                    navHostController.navigate("bookmarkDetail/${event.bookmarkId}")
                }
            }
            viewModel.onNavigationEventConsumed() // Consume the event
        }
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
                    Text("Readeck", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text(text = "All") },
                        selected = false,
                        onClick = { onClickAll() }
                    )
                    NavigationDrawerItem(
                        label = { Text(text = "Unread") },
                        selected = false,
                        onClick = { onClickFilterUnread() }
                    )
                    NavigationDrawerItem(
                        label = { Text(text = "Archive") },
                        selected = false,
                        onClick = { onClickFilterArchive() }
                    )
                    NavigationDrawerItem(
                        label = { Text(text = "Favorites") },
                        selected = false,
                        onClick = { onClickFilterFavorite() }
                    )
                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text(text = "Articles") },
                        selected = false,
                        onClick = { onClickFilterArticles() }
                    )
                    NavigationDrawerItem(
                        label = { Text(text = "Videos") },
                        selected = false,
                        onClick = { onClickFilterVideos() }
                    )
                    NavigationDrawerItem(
                        label = { Text(text = "Pictures") },
                        selected = false,
                        onClick = { onClickFilterPictures() }
                    )
                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text(text = "Collections") },
                        selected = false,
                        onClick = { onClickFilterCollections() }
                    )
                    NavigationDrawerItem(
                        label = { Text(text = "Labels") },
                        selected = false,
                        onClick = { onClickFilterLabels() }
                    )
                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text(text = "Settings") },
                        selected = false,
                        onClick = { onClickSettings() }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Bookmarks") },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } }
                        ) {
                            Icon(Icons.Filled.Menu, contentDescription = "Localized description")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { viewModel.loadBookmarks() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh Bookmarks")
                }
            }
        ) { padding ->
            BookmarkListView(
                modifier = Modifier.padding(padding),
                bookmarks = bookmarks,
                onClickBookmark = onClickBookmark,
                onClickDelete = onClickDelete,
                onClickArchive = onClickArchive,
                onClickFavorite = onClickFavorite,
                onClickMarkRead = onClickMarkRead
            )
        }
    }
}

@Composable
fun BookmarkListView(
    modifier: Modifier = Modifier,
    bookmarks: List<Bookmark>,
    onClickBookmark: (String) -> Unit,
    onClickDelete: (String) -> Unit,
    onClickMarkRead: (String) -> Unit,
    onClickFavorite: (String) -> Unit,
    onClickArchive: (String) -> Unit,
) {
    LazyColumn(modifier = modifier) {
        items(bookmarks) { bookmark ->
            BookmarkCard(bookmark = bookmark,
                onClickCard = onClickBookmark,
                onClickDelete = onClickDelete,
                onClickArchive = onClickArchive,
                onClickFavorite = onClickFavorite,
                onClickMarkRead = onClickMarkRead
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarkListViewPreview() {
    val bookmarks = listOf(
        Bookmark(
            id = "1",
            href = "https://example.com",
            created = kotlinx.datetime.LocalDateTime.parse("2024-01-15T10:00:00"),
            updated = kotlinx.datetime.LocalDateTime.parse("2024-01-16T12:00:00"),
            state = 1,
            loaded = true,
            url = "https://example.com",
            title = "Sample Bookmark",
            siteName = "Example",
            site = "example.com",
            authors = listOf("John Doe"),
            lang = "en",
            textDirection = "ltr",
            documentTpe = "article",
            type = Bookmark.Type.Article,
            hasArticle = true,
            description = "This is a sample bookmark description.",
            isDeleted = false,
            isMarked = false,
            isArchived = false,
            labels = listOf("reading", "tech"),
            readProgress = 50,
            wordCount = 1000,
            readingTime = 10,
            article = Bookmark.Resource(""),
            icon = Bookmark.ImageResource("", 100, 100),
            image = Bookmark.ImageResource("https://via.placeholder.com/150", 150, 150),
            log = Bookmark.Resource(""),
            props = Bookmark.Resource(""),
            thumbnail = Bookmark.ImageResource("", 100, 100),
            articleContent = ""
        )
    )
    // Provide a dummy NavHostController for the preview
    val navController = rememberNavController()
    BookmarkListView(
        modifier = Modifier,
        bookmarks = bookmarks,
        onClickBookmark = {},
        onClickDelete = {},
        onClickArchive = {},
        onClickFavorite = {},
        onClickMarkRead = {}
    )
}
