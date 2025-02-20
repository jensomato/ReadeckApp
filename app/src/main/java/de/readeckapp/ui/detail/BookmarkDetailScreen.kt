package de.readeckapp.ui.detail

import android.text.Html
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

@Composable
fun BookmarkDetailContent(bookmark: Bookmark, modifier: Modifier = Modifier) {
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

        // Main Section (HTML Content - Placeholder)
//        Text(text = bookmark.articleContent ?: "HTML", style = MaterialTheme.typography.bodyMedium)
        // TODO: Implement HTML rendering using a WebView or other suitable component
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = false
                    settings.useWideViewPort = false
                    settings.loadWithOverviewMode = false
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                }
            },
            update = {
                if (bookmark.hasArticle) {
                    if (bookmark.articleContent != null) {
                        val htmlContent = Html.fromHtml(bookmark.articleContent, Html.FROM_HTML_MODE_LEGACY).toString()
                        val content =
                        """
                            <html>
                                <head>
                                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                    <style>
                                        img {
                                            max-width: 100%; /* Make images responsive */
                                            height: auto; /* Maintain aspect ratio */
                                            display: block;  /* prevents space below image */
                                            margin: 0 auto;  /* Center the image */
                                        }
                                        /* Styles for smaller screens (e.g., phones) */
                                        @media (max-width: 600px) {
                                            body {
                                                font-size: 16px; /* Increase font size on small screens */
                                            }

                                            /* Example:  Adjust the padding */
                                            /* Some CSS to prevent over-scrolling if content is bigger than screen width */
                                            .container {
                                                width: 100%;
                                                padding: 10px; /* Adjust padding on smaller screens */
                                            }
                                        }

                                        /* Styles for larger screens (e.g., tablets and desktops) */
                                        @media (min-width: 601px) {
                                            body {
                                                font-size: 14px;
                                            }

                                            /* Example: Wider content area */
                                            .container {
                                                max-width: 800px;  /* Limit width on larger screens */
                                                margin: 0 auto; /* Center the container */
                                                padding: 20px;
                                            }

                                             /* Example: Reduce image size */
                                            img {
                                                max-width: 80%; /* Smaller images on larger screens */
                                            }

                                        }
                                    </style>
                                </head>
                                <body>
                                    <div class="container">
                                    ${bookmark.articleContent}
                                    </div>
                                </body>
                            </html>
                        """
                        it.loadData(content, "text/html", "utf-8")
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
