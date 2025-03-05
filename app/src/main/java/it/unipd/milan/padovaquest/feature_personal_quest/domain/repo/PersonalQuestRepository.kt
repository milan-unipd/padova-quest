package it.unipd.milan.padovaquest.feature_personal_quest.domain.repo

import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest

interface PersonalQuestRepository {
    suspend fun startPersonalQuest(userID: String): Resource<Quest>
    suspend fun cancelPersonalQuest(quest: Quest): Resource<Unit>
}