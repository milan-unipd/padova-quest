package it.unipd.milan.padovaquest.feature_group_quest.domain.use_case

import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.feature_group_quest.domain.model.GroupQuestStatus
import it.unipd.milan.padovaquest.feature_group_quest.domain.repo.GroupQuestRepository
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest
import it.unipd.milan.padovaquest.shared_quests.presentation.service.SharedQuestDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeleteGroupQuestUseCase @Inject constructor(
    private val groupQuestRepository: GroupQuestRepository,
    private val sharedQuestDataRepository: SharedQuestDataRepository
) {

    suspend operator fun invoke(userID: String) = withContext(Dispatchers.IO) {
        sharedQuestDataRepository.setGroupQuestStatus(GroupQuestStatus(isBeingDeleted = true))
        try {
            val result = groupQuestRepository.deleteGroupQuest(userID, sharedQuestDataRepository.quest!!.id!!)
            if (result is Resource.Error)
                throw result.exception
            if (result is Resource.Success)
                sharedQuestDataRepository.quest = Quest()
            sharedQuestDataRepository.setGroupQuestStatus(GroupQuestStatus(wasDeleted = true))
        } catch (e: Exception) {
            sharedQuestDataRepository.setGroupQuestStatus(GroupQuestStatus(exception = e))

        }
    }
}
