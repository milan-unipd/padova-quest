package it.unipd.milan.padovaquest.shared_quests.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.unipd.milan.padovaquest.shared_quests.data.location.LocationClientImpl
import it.unipd.milan.padovaquest.shared_quests.domain.repo.LocationClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

    @Binds
    @Singleton
    abstract fun bindLocationClient(
        locationClientImpl: LocationClientImpl
    ): LocationClient

}