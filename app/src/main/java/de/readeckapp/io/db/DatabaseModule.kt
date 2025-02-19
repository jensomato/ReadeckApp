package de.readeckapp.io.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, ReadeckDatabase::class.java, "readeck.db").build()

    @Provides
    @Singleton
    fun provideBookmarkDao(readeckDatabase: ReadeckDatabase) = readeckDatabase.getBookmarkDao()
}
