package de.readeckapp.domain.mapper

import de.readeckapp.domain.model.Bookmark
import de.readeckapp.io.db.model.BookmarkEntity
import de.readeckapp.io.db.model.ImageResourceEntity
import de.readeckapp.io.db.model.ResourceEntity
import de.readeckapp.io.rest.model.BookmarkDto as BookmarkDto
import de.readeckapp.io.rest.model.Resource as ResourceDto
import de.readeckapp.io.rest.model.ImageResource as ImageResourceDto
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant

fun Bookmark.toEntity(): BookmarkEntity = BookmarkEntity(
    id = id,
    href = href,
    created = created.toInstant(TimeZone.currentSystemDefault()),
    updated = updated.toInstant(TimeZone.currentSystemDefault()),
    state = state,
    loaded = loaded,
    url = url,
    title = title,
    siteName = siteName,
    site = site,
    authors = authors,
    lang = lang,
    textDirection = textDirection,
    documentTpe = documentTpe,
    type = when (type) {
        Bookmark.Type.Article -> BookmarkEntity.Type.Article.value
        Bookmark.Type.Picture -> BookmarkEntity.Type.Picture.value
        Bookmark.Type.Video -> BookmarkEntity.Type.Video.value
    },
    hasArticle = hasArticle,
    description = description,
    isDeleted = isDeleted,
    isMarked = isMarked,
    isArchived = isArchived,
    labels = labels,
    readProgress = readProgress,
    wordCount = wordCount,
    readingTime = readingTime,
    article = article.toEntity(),
    icon = icon.toEntity(),
    image = image.toEntity(),
    log = log.toEntity(),
    props = props.toEntity(),
    thumbnail = thumbnail.toEntity(),
    articleContent = articleContent
)

fun Bookmark.Resource.toEntity(): ResourceEntity = ResourceEntity(
    src = this.src
)

fun Bookmark.ImageResource.toEntity(): ImageResourceEntity = ImageResourceEntity(
    src = this.src,
    width = this.width,
    height = this.height
)

fun BookmarkEntity.toDomain(): Bookmark = Bookmark(
    id = id,
    href = href,
    created = created.toLocalDateTime(TimeZone.currentSystemDefault()),
    updated = updated.toLocalDateTime(TimeZone.currentSystemDefault()),
    state = state,
    loaded = loaded,
    url = url,
    title = title,
    siteName = siteName,
    site = site,
    authors = authors,
    lang = lang,
    textDirection = textDirection,
    documentTpe = documentTpe,
    type = when (type) {
        BookmarkEntity.Type.Article.value -> Bookmark.Type.Article
        BookmarkEntity.Type.Picture.value -> Bookmark.Type.Picture
        BookmarkEntity.Type.Video.value -> Bookmark.Type.Video
        else -> Bookmark.Type.Article
    },
    hasArticle = hasArticle,
    description = description,
    isDeleted = isDeleted,
    isMarked = isMarked,
    isArchived = isArchived,
    labels = labels,
    readProgress = readProgress,
    wordCount = wordCount,
    readingTime = readingTime,
    article = article.toDomain(),
    icon = icon.toDomain(),
    image = image.toDomain(),
    log = log.toDomain(),
    props = props.toDomain(),
    thumbnail = thumbnail.toDomain(),
    articleContent = articleContent
)

fun ResourceEntity.toDomain(): Bookmark.Resource = Bookmark.Resource(
    src = this.src
)

fun ImageResourceEntity.toDomain(): Bookmark.ImageResource = Bookmark.ImageResource(
    src = this.src,
    width = this.width,
    height = this.height
)

fun BookmarkDto.toDomain(): Bookmark = Bookmark(
    id = id,
    href = href,
    created = created.toLocalDateTime(TimeZone.currentSystemDefault()),
    updated = updated.toLocalDateTime(TimeZone.currentSystemDefault()),
    state = state,
    loaded = loaded,
    url = url,
    title = title,
    siteName = siteName,
    site = site,
    authors = authors,
    lang = lang,
    textDirection = textDirection,
    documentTpe = documentTpe,
    type = when (type) {
        BookmarkEntity.Type.Article.value -> Bookmark.Type.Article
        BookmarkEntity.Type.Picture.value -> Bookmark.Type.Picture
        BookmarkEntity.Type.Video.value -> Bookmark.Type.Video
        else -> Bookmark.Type.Article
    },
    hasArticle = hasArticle,
    description = description,
    isDeleted = isDeleted,
    isMarked = isMarked,
    isArchived = isArchived,
    labels = labels,
    readProgress = readProgress,
    wordCount = wordCount,
    readingTime = readingTime,
    article = resources.article.toDomain(),
    icon = resources.icon.toDomain(),
    image = resources.image.toDomain(),
    log = resources.log.toDomain(),
    props = resources.props.toDomain(),
    thumbnail = resources.thumbnail.toDomain(),
    articleContent = null
)

fun ResourceDto?.toDomain(): Bookmark.Resource = Bookmark.Resource(
    src = this?.src ?: ""
)

fun ImageResourceDto?.toDomain(): Bookmark.ImageResource = Bookmark.ImageResource(
    src = this?.src ?: "",
    width = this?.width ?: 0,
    height = this?.height ?: 0
)
