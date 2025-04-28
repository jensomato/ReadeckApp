package de.readeckapp.io.db.model

import androidx.room.Embedded
import androidx.room.Relation

data class BookmarkWithArticleContent(
    @Embedded
    val bookmark: BookmarkEntity,
    @Relation(
        entity = ArticleContentEntity::class,
        parentColumn = "id",
        entityColumn = "bookmarkId"

    )
    val articleContent: ArticleContentEntity?
)
