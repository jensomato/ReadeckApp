package de.readeckapp

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.readeckapp.coroutine.ApplicationScope
import de.readeckapp.domain.BookmarkRepository
import de.readeckapp.domain.BookmarkRepositoryImpl
import de.readeckapp.domain.usecase.LoadArticleUseCase
import de.readeckapp.io.rest.NetworkModule
import de.readeckapp.io.rest.ReadeckApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module(includes = [NetworkModule::class])
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    abstract fun bindBookmarkRepository(bookmarkRepositoryImpl: BookmarkRepositoryImpl): BookmarkRepository

    companion object {
        @Provides
        fun provideLoadBookmarksUseCase(
            bookmarkRepository: BookmarkRepository,
            readeckApi: ReadeckApi
        ): LoadArticleUseCase {
            return LoadArticleUseCase(bookmarkRepository, readeckApi)
        }

        @Singleton
        @Provides
        @ApplicationScope
        fun provideApplicationScope(): CoroutineScope {
            return CoroutineScope(Dispatchers.IO + SupervisorJob())
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object WorkManagerInitializer : Initializer<WorkManager> {

    @Provides
    @Singleton
    override fun create(@ApplicationContext context: Context): WorkManager {
        val configuration = Configuration.Builder().build()
//        WorkManager.initialize(context, configuration)
        Log.d("Hilt Init", "WorkManager initialized by Hilt this time")
        return WorkManager.getInstance(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        // No dependencies on other libraries.
        return emptyList()
    }
}