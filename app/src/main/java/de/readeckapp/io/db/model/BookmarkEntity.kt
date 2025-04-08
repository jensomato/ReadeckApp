package de.readeckapp.io.db.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(
    tableName = "bookmarks",
    indices = [
        Index(value = ["readProgress"]),
        Index(value = ["type"]),
        Index(value = ["isArchived"]),
        Index(value = ["isMarked"])
    ]
)
data class BookmarkEntity(
    @PrimaryKey
    val id: String,
    val href: String,
    val created: Instant,
    val updated: Instant,
    val state: State,
    val loaded: Boolean,
    val url: String,
    val title: String,
    val siteName: String,
    val site: String,
    val authors: List<String>,
    val lang: String,
    val textDirection: String,
    val documentTpe: String,
    val type: String,
    val hasArticle: Boolean,
    val description: String,
    val isDeleted: Boolean,
    val isMarked: Boolean,
    val isArchived: Boolean,
    val labels: List<String>,
    val readProgress: Int,
    val wordCount: Int?,
    val readingTime: Int?,

    // Embedded Resources
    @Embedded(prefix = "article_")
    val article: ResourceEntity,
    @Embedded(prefix = "icon_")
    val icon: ImageResourceEntity,
    @Embedded(prefix = "image_")
    val image: ImageResourceEntity,
    @Embedded(prefix = "log_")
    val log: ResourceEntity,
    @Embedded(prefix = "props_")
    val props: ResourceEntity,
    @Embedded(prefix = "thumbnail_")
    val thumbnail: ImageResourceEntity,
    val articleContent: String?
) {
    sealed class Type(val value: String) {
        data object Article: Type("article")
        data object Video: Type("video")
        data object Picture: Type("picture")
    }
    enum class State(val value: Int) {
        LOADED(0),
        ERROR(1),
        LOADING(2)
    }
}
