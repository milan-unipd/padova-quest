package it.unipd.milan.padovaquest.feature_walking.di

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.unipd.milan.padovaquest.feature_walking.data.WalkRepositoryImpl
import it.unipd.milan.padovaquest.feature_walking.domain.repo.WalkRepository
import it.unipd.milan.padovaquest.feature_walking.domain.use_case.GetPlaceDescriptionUseCase
import it.unipd.milan.padovaquest.feature_walking.domain.use_case.GetPlacesWithinBoundsUseCase
import it.unipd.milan.padovaquest.feature_walking.domain.use_case.SetPlaceSeenUseCase
import it.unipd.milan.padovaquest.feature_walking.domain.use_case.WalkUseCases
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class WalkModule {

    @Provides
    @Singleton
    fun provideWalkRepo(firestore: FirebaseFirestore): WalkRepository {
        return WalkRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun providePlacesUseCases(walkRepository: WalkRepository): WalkUseCases {
        return WalkUseCases(
            getPlacesWithinBoundsUseCase = GetPlacesWithinBoundsUseCase(walkRepository),
            setPlaceSeenUseCase = SetPlaceSeenUseCase(walkRepository),
            getPlaceDescriptionUseCase = GetPlaceDescriptionUseCase(walkRepository)
        )
    }
}