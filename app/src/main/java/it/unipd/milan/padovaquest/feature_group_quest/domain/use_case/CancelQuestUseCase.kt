package it.unipd.milan.padovaquest.feature_group_quest.domain.use_case

import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.feature_group_quest.domain.model.GroupQuestStatus
import it.unipd.milan.padovaquest.feature_group_quest.domain.repo.GroupQuestRepository
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest
import it.unipd.milan.padovaquest.shared_quests.presentation.service.SharedQuestDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CancelQuestUseCase @Inject constructor(
    private val groupQuestRepository: GroupQuestRepository,
    private val sharedQuestDataRepository: SharedQuestDataRepository
) {

    suspend operator fun invoke(userID: String) = withContext(Dispatchers.IO) {
        flow {
            emit(Resource.Loading)
            try {
                val quest = sharedQuestDataRepository.quest

                if (quest?.id == null)
                    return@flow

                val result = groupQuestRepository.cancelGroupQuest(userID, quest.id!!)
                if (result is Resource.Error)
                    throw result.exception

                if (result is Resource.Success) {
                    sharedQuestDataRepository.quest = Quest()
                    sharedQuestDataRepository.setGroupQuestStatus(GroupQuestStatus())
                    emit(Resource.Success(Unit))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                sharedQuestDataRepository.setGroupQuestStatus(GroupQuestStatus(exception = e))

            }
        }
    }
}
