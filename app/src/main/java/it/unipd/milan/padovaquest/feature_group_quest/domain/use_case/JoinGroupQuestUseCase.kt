package it.unipd.milan.padovaquest.feature_group_quest.domain.use_case

import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.feature_group_quest.domain.model.GroupQuestStatus
import it.unipd.milan.padovaquest.feature_group_quest.domain.repo.GroupQuestRepository
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest
import it.unipd.milan.padovaquest.shared_quests.presentation.service.SharedQuestDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class JoinGroupQuestUseCase @Inject constructor(
    private val groupQuestRepository: GroupQuestRepository,
    private val sharedQuestDataRepository: SharedQuestDataRepository
) {

    suspend operator fun invoke(userID: String, questID: String) =
        withContext(Dispatchers.IO) {

            sharedQuestDataRepository.setGroupQuestStatus(GroupQuestStatus(isJoining = true))
            val response = groupQuestRepository.joinGroupQuest(userID, questID,
                onQuestStartedAction = {
                    sharedQuestDataRepository.setGroupQuestStatus(GroupQuestStatus(hasStarted = true))
                },
                onQuestDeleteAction = {
                    sharedQuestDataRepository.setGroupQuestStatus(GroupQuestStatus(wasDeleted = true))
                    sharedQuestDataRepository.quest = Quest()
                },
                onErrorAction = { exception ->
                    sharedQuestDataRepository.setGroupQuestStatus(GroupQuestStatus(exception = exception))
                }
            )

            if (response is Resource.Success) {
                sharedQuestDataRepository.quest = response.result
                sharedQuestDataRepository.setGroupQuestStatus(GroupQuestStatus(isWaitingToStart = true))
            } else {
                sharedQuestDataRepository.quest = Quest()
                sharedQuestDataRepository.setGroupQuestStatus(GroupQuestStatus(exception = (response as Resource.Error).exception))
            }
        }
}
