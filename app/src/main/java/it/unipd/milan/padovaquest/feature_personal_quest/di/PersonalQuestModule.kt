package it.unipd.milan.padovaquest.feature_personal_quest.di

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.unipd.milan.padovaquest.feature_personal_quest.data.PersonalQuestRepoImpl
import it.unipd.milan.padovaquest.feature_personal_quest.domain.repo.PersonalQuestRepository
import it.unipd.milan.padovaquest.feature_personal_quest.domain.use_case.CancelQuestUseCase
import it.unipd.milan.padovaquest.feature_personal_quest.domain.use_case.PersonalQuestUseCases
import it.unipd.milan.padovaquest.feature_personal_quest.domain.use_case.StartPersonalQuestUseCase
import it.unipd.milan.padovaquest.shared_quests.domain.repo.QuestRepository
import it.unipd.milan.padovaquest.shared_quests.presentation.service.SharedQuestDataRepository
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class PersonalQuestModule {

    @Provides
    @Singleton
    fun providePersonalQuestRepo(firestore: FirebaseFirestore, questRepository: QuestRepository): PersonalQuestRepository {
        return PersonalQuestRepoImpl(firestore, questRepository)
    }

    @Provides
    @Singleton
    fun providePersonalQuestUseCases(personalQuestRepository: PersonalQuestRepository, sharedQuestDataRepository: SharedQuestDataRepository): PersonalQuestUseCases {
        return PersonalQuestUseCases(
            startPersonalQuestUseCase = StartPersonalQuestUseCase(personalQuestRepository, sharedQuestDataRepository),
            cancelQuestUseCase = CancelQuestUseCase(personalQuestRepository, sharedQuestDataRepository)
        )
    }
}