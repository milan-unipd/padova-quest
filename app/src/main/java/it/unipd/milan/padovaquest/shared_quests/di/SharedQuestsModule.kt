package it.unipd.milan.padovaquest.shared_quests.di

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.unipd.milan.padovaquest.feature_walking.domain.repo.WalkRepository
import it.unipd.milan.padovaquest.shared_quests.data.PlacesRepoImpl
import it.unipd.milan.padovaquest.shared_quests.data.QuestRepoImpl
import it.unipd.milan.padovaquest.shared_quests.domain.repo.PlacesRepository
import it.unipd.milan.padovaquest.shared_quests.domain.repo.QuestRepository
import it.unipd.milan.padovaquest.shared_quests.domain.use_case.AnswerQuestionUseCase
import it.unipd.milan.padovaquest.shared_quests.domain.use_case.GetCurrentQuestUseCase
import it.unipd.milan.padovaquest.shared_quests.domain.use_case.GetNearestPlaceUseCase
import it.unipd.milan.padovaquest.shared_quests.domain.use_case.PlacesUseCases
import it.unipd.milan.padovaquest.shared_quests.domain.use_case.QuestUseCases
import it.unipd.milan.padovaquest.shared_quests.presentation.service.SharedQuestDataRepository
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object SharedQuestsModule {


    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun providePlacesRepo(firestore: FirebaseFirestore, walkRepository: WalkRepository): PlacesRepository {
        return PlacesRepoImpl(firestore, walkRepository)
    }

    @Provides
    @Singleton
    fun providePlacesUseCases(placesRepository: PlacesRepository): PlacesUseCases {
        return PlacesUseCases(
            getNearestPlaceUseCase = GetNearestPlaceUseCase(placesRepository)
        )
    }

    @Provides
    @Singleton
    fun provideQuestRepo(firestore: FirebaseFirestore): QuestRepository {
        return QuestRepoImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideQuestUseCases(questRepository: QuestRepository, sharedQuestDataRepository: SharedQuestDataRepository): QuestUseCases {
        return QuestUseCases(
            getCurrentQuestUseCase = GetCurrentQuestUseCase(questRepository, sharedQuestDataRepository),
            answerQuestionUseCase = AnswerQuestionUseCase(questRepository, sharedQuestDataRepository)
        )
    }
}