package de.readeckapp.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.google.android.material.chip.Chip
import de.readeckapp.R
import de.readeckapp.domain.model.Bookmark
import de.readeckapp.domain.model.BookmarkListItem
import de.readeckapp.ui.components.ErrorPlaceholderImage
import timber.log.Timber

@Composable
fun BookmarkCard(
    bookmark: BookmarkListItem,
    onClickCard: (String) -> Unit,
    onClickDelete: (String) -> Unit,
    onClickMarkRead: (String, Boolean) -> Unit,
    onClickFavorite: (String, Boolean) -> Unit,
    onClickShareBookmark: (String) -> Unit,
    onClickArchive: (String, Boolean) -> Unit,
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
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(bookmark.imageSrc)
                    .crossfade(true).build(),
                contentDescription = stringResource(R.string.common_bookmark_image_content_description),
                contentScale = ContentScale.FillWidth,
                error = {
                    ErrorPlaceholderImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        imageContentDescription = stringResource(R.string.common_bookmark_image_content_description)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
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
                        model = ImageRequest.Builder(LocalContext.current).data(bookmark.iconSrc)
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

                if(bookmark.labels.isNotEmpty()){
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ){
                        Icon(
                            painter = painterResource(R.drawable.ic_label_24px),
                            contentDescription = "labels"
                        )
                        Spacer(Modifier.width(8.dp))

                        FadingLabelRow(items = bookmark.labels)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ){
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                onClickFavorite(bookmark.id, !bookmark.isMarked)
                            }
                        ){
                            Icon(
                                imageVector = if (bookmark.isMarked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = stringResource(R.string.action_favorite),
                                modifier = Modifier.alpha(if(bookmark.isMarked) 1f else 0.5f)
                            )
                        }

                        IconButton(
                            onClick = {
                                onClickArchive(bookmark.id, !bookmark.isArchived)
                            }
                        ){
                            Icon(
                                imageVector = if (bookmark.isArchived) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2,
                                contentDescription = stringResource(R.string.action_archive),
                                modifier = Modifier.alpha(if(bookmark.isArchived) 1f else 0.5f)

                            )
                        }


                    }

                    Spacer(Modifier.weight(1f))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
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
                                text = { Text(stringResource(R.string.action_mark_read)) },
                                onClick = {
                                    onClickMarkRead(bookmark.id, !bookmark.isRead)
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (bookmark.isRead) Icons.Filled.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                        contentDescription = stringResource(R.string.action_mark_read)
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_share)) },
                                onClick = {
                                    onClickShareBookmark(bookmark.url)
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
                                    onClickDelete(bookmark.id)
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


                /*
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

                    }
                }*/

            }
        }
    }
}

@Composable
fun FadingLabelRow(items: List<String>) {
    val scrollState = rememberScrollState()
    val endFadeWidth = 48.dp
    val minRowHeight = 24.dp

    // Calculate alpha value based on scrollState
    val fadeAlpha by remember {
        derivedStateOf {
            if (scrollState.maxValue == 0) {
                0f
            } else {
                val remainingScroll = scrollState.maxValue - scrollState.value
                (remainingScroll.toFloat() / (endFadeWidth.value * 2)).coerceIn(0f, 1f)
            }
        }
    }

    Box (
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // the actual Row with the label chips
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .padding(end = endFadeWidth)
        ) {
            items.forEach {item ->
                SuggestionChip(
                    onClick = {},
                    label = { Text(
                        item,
                        style = MaterialTheme.typography.labelLarge
                    ) },
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(minRowHeight)
                )
            }
        }
        // add a Box with a gradient if there's still scrollable area
        if (fadeAlpha > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(endFadeWidth)
                    .fillMaxHeight()
                    .alpha(fadeAlpha)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        )
                    )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarkCardPreview() {
    val previewHandler = AsyncImagePreviewHandler {
        ColorImage(Color.Red.toArgb())
    }
    val sampleBookmark = BookmarkListItem(
        id = "1",
        url = "https://sample.url",
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
    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
        BookmarkCard(
            bookmark = sampleBookmark,
            onClickCard = {},
            onClickDelete = {},
            onClickMarkRead = { _, _ -> },
            onClickFavorite = { _, _ -> },
            onClickArchive = { _, _ -> },
            onClickShareBookmark = {_ -> }
        )
    }
}
