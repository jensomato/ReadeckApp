package de.readeckapp.io.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_bookmark_ids")
data class RemoteBookmarkIdEntity(
    @PrimaryKey val id: String
)