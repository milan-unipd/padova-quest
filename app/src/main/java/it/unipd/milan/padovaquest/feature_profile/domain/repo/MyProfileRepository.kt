package it.unipd.milan.padovaquest.feature_profile.domain.repo

import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.feature_profile.data.model.GroupQuestResultModel
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest

interface MyProfileRepository {

    suspend fun getQuests(userID:String, type:String, startIndex: Int, pageSize: Int): Resource<List<Quest>>
    suspend fun getGroupQuestResults(questID: String): Resource<GroupQuestResultModel>

    suspend fun updateUserName(userID: String, newName: String): Resource<Unit>
}