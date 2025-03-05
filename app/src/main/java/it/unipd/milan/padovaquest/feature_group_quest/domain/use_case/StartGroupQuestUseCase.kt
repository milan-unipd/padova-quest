package it.unipd.milan.padovaquest.feature_group_quest.domain.use_case

import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.feature_group_quest.domain.model.GroupQuestStatus
import it.unipd.milan.padovaquest.feature_group_quest.domain.repo.GroupQuestRepository
import it.unipd.milan.padovaquest.shared_quests.presentation.service.SharedQuestDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StartGroupQuestUseCase @Inject constructor(
    private val groupQuestRepository: GroupQuestRepository,
    private val sharedQuestDataRepository: SharedQuestDataRepository
) {

    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        sharedQuestDataRepository.setGroupQuestStatus(GroupQuestStatus(isStarting = true))
        try {
            val result = groupQuestRepository.startGroupQuest(sharedQuestDataRepository.quest!!.id!!)
            if (result is Resource.Error)
                throw result.exception
            if (result is Resource.Success)
                sharedQuestDataRepository.quest!!.status = "started"
            sharedQuestDataRepository.setGroupQuestStatus(GroupQuestStatus(hasStarted = true))
        } catch (e: Exception) {
            sharedQuestDataRepository.setGroupQuestStatus(GroupQuestStatus(exception = e))

        }
    }
}