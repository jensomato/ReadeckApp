package de.readeckapp.io.db.model

data class ResourceEntity(
    val src: String
)

data class ImageResourceEntity(
    val src: String,
    val width: Int,
    val height: Int
)
