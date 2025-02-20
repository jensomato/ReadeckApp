package de.readeckapp.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.request.ImageRequest
import coil3.request.crossfade
import de.readeckapp.R
import de.readeckapp.domain.model.Bookmark
import timber.log.Timber

@Composable
fun BookmarkCard(
    bookmark: Bookmark,
    onClickCard: (String) -> Unit,
    onClickDelete: (String) -> Unit,
    onClickMarkRead: (String) -> Unit,
    onClickFavorite: (String) -> Unit,
    onClickArchive: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClickCard(bookmark.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(bookmark.image.src)
                    .crossfade(true).build(),
                contentDescription = "Bookmark Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                onLoading = { Timber.d("loading ${bookmark.image.src}") },
                onError = { Timber.d("error ${bookmark.image.src}") },
                onSuccess = { Timber.d("success ${bookmark.image.src}") },
            )

            // Title, Date, and Labels
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = bookmark.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis

                )
                HorizontalDivider(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(bookmark.icon.src)
                            .crossfade(true).build(),
                        contentDescription = "site icon",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .width(16.dp)
                            .height(16.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = bookmark.siteName, style = MaterialTheme.typography.titleSmall)

                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (bookmark.labels.isNotEmpty()) {
                            Icon(
                                painter = painterResource(R.drawable.ic_label_24px),
                                contentDescription = "labels"
                            )
                            Spacer(Modifier.width(8.dp))
                            val labels = bookmark.labels.fold("") { acc, label ->
                                if (acc.isNotEmpty()) {
                                    "$acc, $label"
                                } else {
                                    label
                                }
                            }
                            Text(
                                text = labels,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(end = 4.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Box(
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        IconButton(
                            onClick = {
                                expanded = true
                            },
                        ) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Actions")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Favorite") },
                                onClick = {
                                    onClickFavorite(bookmark.id)
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Favorite,
                                        contentDescription = "Favorite"
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Archive") },
                                onClick = {
                                    onClickArchive(bookmark.id)
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.DateRange,
                                        contentDescription = "Archive"
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Mark Read") },
                                onClick = {
                                    onClickMarkRead(bookmark.id)
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = "Mark Read"
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    onClickDelete(bookmark.id)
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete"
                                    )
                                }
                            )
                        }
                    }
                }

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarkCardPreview() {
    val previewHandler = AsyncImagePreviewHandler {
        ColorImage(Color.Red.toArgb())
    }
    val sampleBookmark = Bookmark(
        id = "1",
        href = "https://example.com",
        created = kotlinx.datetime.LocalDateTime.parse("2024-01-15T10:00:00"), // Use LocalDateTime
        updated = kotlinx.datetime.LocalDateTime.parse("2024-01-16T12:00:00"), // Use LocalDateTime
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
        labels = listOf(
            "one",
            "two",
            "three",
            "fourhundretandtwentyone",
            "threethousendtwohundretandfive"
        ),
        readProgress = 50,
        wordCount = 1000,
        readingTime = 10,
        article = Bookmark.Resource(""),
        icon = Bookmark.ImageResource("", 100, 100),
        image = Bookmark.ImageResource("https://picsum.photos/seed/picsum/640/480", 150, 150),
        log = Bookmark.Resource(""),
        props = Bookmark.Resource(""),
        thumbnail = Bookmark.ImageResource("", 100, 100),
        articleContent = ""
    )
    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
        BookmarkCard(
            bookmark = sampleBookmark,
            onClickCard = {},
            onClickDelete = {},
            onClickMarkRead = {},
            onClickFavorite = {},
            onClickArchive = {}
        )
    }
}
