package it.unipd.milan.padovaquest.feature_group_quest.domain.repo

import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest

interface GroupQuestRepository {

    suspend fun createGroupQuest(userID: String): Resource<Quest>

    suspend fun startGroupQuest(questID: String): Resource<Unit>

    suspend fun deleteGroupQuest(userID: String, questID: String): Resource<Unit>

    suspend fun cancelGroupQuest(userID: String, questID: String): Resource<Unit>

    suspend fun joinGroupQuest(
        userID: String,
        questID: String,
        onQuestStartedAction: () -> Unit,
        onQuestDeleteAction: () -> Unit,
        onErrorAction: (Exception) -> Unit
    ): Resource<Quest?>

    suspend fun leaveGroupQuest(userID: String, questID: String): Resource<Unit>


}