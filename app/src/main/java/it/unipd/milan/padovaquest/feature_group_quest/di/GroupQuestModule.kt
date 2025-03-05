package it.unipd.milan.padovaquest.feature_group_quest.di

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.unipd.milan.padovaquest.feature_group_quest.data.GroupQuestRepoImpl
import it.unipd.milan.padovaquest.feature_group_quest.domain.repo.GroupQuestRepository
import it.unipd.milan.padovaquest.feature_group_quest.domain.use_case.CancelQuestUseCase
import it.unipd.milan.padovaquest.feature_group_quest.domain.use_case.CreateGroupQuestUseCase
import it.unipd.milan.padovaquest.feature_group_quest.domain.use_case.DeleteGroupQuestUseCase
import it.unipd.milan.padovaquest.feature_group_quest.domain.use_case.GroupQuestUseCases
import it.unipd.milan.padovaquest.feature_group_quest.domain.use_case.JoinGroupQuestUseCase
import it.unipd.milan.padovaquest.feature_group_quest.domain.use_case.LeaveGroupQuestUseCase
import it.unipd.milan.padovaquest.feature_group_quest.domain.use_case.StartGroupQuestUseCase
import it.unipd.milan.padovaquest.shared_quests.domain.repo.QuestRepository
import it.unipd.milan.padovaquest.shared_quests.presentation.service.SharedQuestDataRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class GroupQuestModule {


    @Provides
    @Singleton
    fun provideGroupQuestRepository(firestore: FirebaseFirestore, questRepository: QuestRepository): GroupQuestRepository {
        return GroupQuestRepoImpl(firestore, questRepository)
    }

    @Provides
    @Singleton
    fun provideGroupQuestUseCases(groupQuestRepository: GroupQuestRepository, sharedQuestDataRepository: SharedQuestDataRepository): GroupQuestUseCases {
        return GroupQuestUseCases(
            createGroupQuestUseCase = CreateGroupQuestUseCase(groupQuestRepository, sharedQuestDataRepository),
            startGroupQuestUseCase = StartGroupQuestUseCase(groupQuestRepository, sharedQuestDataRepository),
            deleteGroupQuestUseCase = DeleteGroupQuestUseCase(groupQuestRepository, sharedQuestDataRepository),
            cancelQuestUseCase = CancelQuestUseCase(groupQuestRepository, sharedQuestDataRepository),
            joinGroupQuestUseCase = JoinGroupQuestUseCase(groupQuestRepository, sharedQuestDataRepository),
            leaveGroupQuestUseCase = LeaveGroupQuestUseCase(groupQuestRepository, sharedQuestDataRepository)
        )
    }
}