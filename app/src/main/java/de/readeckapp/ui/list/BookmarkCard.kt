package de.readeckapp.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import de.readeckapp.domain.model.Bookmark
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun BookmarkCard(bookmark: Bookmark, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick(bookmark.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(bookmark.image.src).crossfade(true).build(),
                contentDescription = "Bookmark Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            // Title, Date, and Labels
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = bookmark.title, style = MaterialTheme.typography.titleMedium) // Use titleMedium
                val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
                val formattedDate = bookmark.created.toJavaLocalDateTime().format(formatter)
                Text(text = "Created: $formattedDate", style = MaterialTheme.typography.bodySmall)

                // Labels
                if (bookmark.labels.isNotEmpty()) {
                    Row(modifier = Modifier.padding(top = 4.dp)) { bookmark.labels.forEach { label -> Text(text = label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(end = 4.dp)) }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarkCardPreview() {
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
    BookmarkCard(bookmark = sampleBookmark, onClick = {})
}
