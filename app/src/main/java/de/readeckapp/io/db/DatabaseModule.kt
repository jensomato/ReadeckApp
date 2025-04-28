package de.readeckapp.io.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.readeckapp.io.db.dao.BookmarkDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ReadeckDatabase {
        return Room.databaseBuilder(context, ReadeckDatabase::class.java, "readeck.db")
            .addMigrations(ReadeckDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun provideBookmarkDao(readeckDatabase: ReadeckDatabase): BookmarkDao = readeckDatabase.getBookmarkDao()
}
