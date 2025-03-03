package de.readeckapp.ui.detail

import android.view.View
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import de.readeckapp.domain.model.Bookmark
import timber.log.Timber
import java.io.InputStreamReader
import java.nio.charset.Charset
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkDetailScreen(navController: NavController, bookmarkId: String?) {
    val viewModel: BookmarkDetailViewModel = hiltViewModel()
    val bookmark = viewModel.bookmark.collectAsState().value

    LaunchedEffect(bookmarkId) {
        if (bookmarkId != null) {
            viewModel.loadBookmark(bookmarkId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(bookmark?.title ?: "Bookmark Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (bookmark != null) {
                BookmarkDetailMenu(bookmark = bookmark, viewModel = viewModel)
            }
        }
    ) { padding ->
        if (bookmark != null) {
            BookmarkDetailContent(bookmark = bookmark, modifier = Modifier.padding(padding))
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalEncodingApi::class)
@Composable
fun BookmarkDetailContent(bookmark: Bookmark, modifier: Modifier = Modifier) {
    val assetManager = LocalContext.current.assets
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Section
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(bookmark.image.src).crossfade(true).build(),
            contentDescription = "Bookmark Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = bookmark.title, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            bookmark.labels.forEach { label ->
                Text(text = label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(end = 4.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = false
                    settings.useWideViewPort = false
                    settings.loadWithOverviewMode = false
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    settings.defaultTextEncodingName = "utf-8"
                }
            },
            update = {
                if (bookmark.hasArticle) {
                    if (bookmark.articleContent != null) {
                        val htmlContent = bookmark.articleContent
                        val inputStream = assetManager.open("html_template.html")
                        val reader = InputStreamReader(inputStream)
                        val template = reader.readText()
                        reader.close()
                        val content = template.replace("%s", htmlContent)
                        val encodedHtml = Base64.withPadding(Base64.PaddingOption.ABSENT).encode(content.toByteArray())
                        it.loadData(encodedHtml, "text/html", "base64")
                    }
                }
            }
        )
    }
}

@Composable
fun BookmarkDetailMenu(bookmark: Bookmark, viewModel: BookmarkDetailViewModel) {
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
                text = { Text("Favorite") },
                onClick = {
                    viewModel.toggleFavorite(bookmark)
                    expanded = false
                },
                leadingIcon = { Icon(Icons.Filled.Favorite, contentDescription = "Favorite") }
            )
            DropdownMenuItem(
                text = { Text("Archive") },
                onClick = {
                    viewModel.toggleArchive(bookmark)
                    expanded = false
                },
                leadingIcon = { Icon(Icons.Filled.DateRange, contentDescription = "Archive") }
            )
            DropdownMenuItem(
                text = { Text("Mark Read") },
                onClick = {
                    viewModel.markRead(bookmark)
                    expanded = false
                },
                leadingIcon = { Icon(Icons.Filled.Check, contentDescription = "Mark Read") }
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    viewModel.deleteBookmark(bookmark)
                    expanded = false
                },
                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
            )
        }
    }
}
