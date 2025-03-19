package de.readeckapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import de.readeckapp.io.AssetLoader
import de.readeckapp.io.AssetLoaderImpl

@Module
@InstallIn(ViewModelComponent::class)
interface ViewModelModule {
    @Binds
    fun bindAssetLoader(assetLoaderImpl: AssetLoaderImpl): AssetLoader
}
