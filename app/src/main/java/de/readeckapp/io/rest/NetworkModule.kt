package de.readeckapp.io.rest

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.readeckapp.io.prefs.SettingsDataStore
import de.readeckapp.io.prefs.SettingsDataStoreImpl
import de.readeckapp.io.rest.auth.AuthInterceptor
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        baseUrlInterceptor: UrlInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(baseUrlInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val mediaType = "application/json; charset=UTF8".toMediaType()
        return Retrofit.Builder()
            .baseUrl("http://readeck.invalid/")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(Json.asConverterFactory(mediaType))
            .build()
    }

    @Provides
    @Singleton
    fun provideReadeckApiService(retrofit: Retrofit): ReadeckApi {
        return retrofit.create()
    }

    @Provides
    @Singleton
    fun provideSettingsDataStore(impl: SettingsDataStoreImpl): SettingsDataStore {
        return impl
    }
}
