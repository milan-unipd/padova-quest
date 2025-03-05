package it.unipd.milan.padovaquest.shared_quests.domain.use_case

import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.feature_group_quest.domain.model.GroupQuestStatus
import it.unipd.milan.padovaquest.shared_quests.domain.repo.QuestRepository
import it.unipd.milan.padovaquest.shared_quests.presentation.service.SharedQuestDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetCurrentQuestUseCase @Inject constructor(
    private val questRepository: QuestRepository,
    private val sharedQuestDataRepository: SharedQuestDataRepository
) {
    suspend operator fun invoke(userID: String) = withContext(Dispatchers.IO) {
        flow {
            emit(Resource.Loading)
            val response = questRepository.getCurrentQuest(userID)
            if (response is Resource.Success) {
                val quest = response.result
                sharedQuestDataRepository.quest = quest
                if (quest?.type == "group") {
                    if (quest.status == "created") {
                        if (quest.createdBy == userID)
                            sharedQuestDataRepository.setGroupQuestStatus(GroupQuestStatus(hasBeenCreated = true))
                        else{
                            sharedQuestDataRepository.setGroupQuestStatus(GroupQuestStatus(isWaitingToStart = true))
                        }
                    } else if (quest.status == "started")
                        sharedQuestDataRepository.setGroupQuestStatus(GroupQuestStatus(hasStarted = true))
                }
            }
            emit(response)
        }
    }
}