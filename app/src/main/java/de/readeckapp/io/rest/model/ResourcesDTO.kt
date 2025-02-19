package de.readeckapp.io.rest.model

data class ResourcesDTO(
    val article: ArticleResourceDTO? = null,
    val icon: IconResourceDTO? = null,
    val image: ImageResourceDTO? = null,
    val log: LogResourceDTO? = null,
    val props: PropsResourceDTO? = null,
    val thumbnail: ThumbnailResourceDTO? = null
)
