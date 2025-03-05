package it.unipd.milan.padovaquest.feature_profile.di

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.unipd.milan.padovaquest.feature_profile.data.MyProfileRepoImpl
import it.unipd.milan.padovaquest.feature_profile.domain.repo.MyProfileRepository
import it.unipd.milan.padovaquest.feature_profile.domain.use_cases.GetGroupQuestResultsUseCase
import it.unipd.milan.padovaquest.feature_profile.domain.use_cases.GetQuestsUseCase
import it.unipd.milan.padovaquest.feature_profile.domain.use_cases.MyProfileUseCases
import it.unipd.milan.padovaquest.feature_profile.domain.use_cases.UpdateNameUseCase
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class MyQuestsModule {

    @Provides
    @Singleton
    fun provideMyQuestsRepository(firestore: FirebaseFirestore): MyProfileRepository {
        return MyProfileRepoImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideMyProfileUseCases(myProfileRepository: MyProfileRepository): MyProfileUseCases {
        return MyProfileUseCases(
            getQuestsUseCase = GetQuestsUseCase(myProfileRepository),
            getGroupQuestResultsUseCase = GetGroupQuestResultsUseCase(myProfileRepository),
            updateNameUseCase = UpdateNameUseCase(myProfileRepository)
        )
    }

}