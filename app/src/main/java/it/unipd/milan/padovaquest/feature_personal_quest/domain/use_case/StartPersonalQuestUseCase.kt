package it.unipd.milan.padovaquest.feature_personal_quest.domain.use_case

import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.feature_personal_quest.domain.repo.PersonalQuestRepository
import it.unipd.milan.padovaquest.shared_quests.presentation.service.SharedQuestDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StartPersonalQuestUseCase @Inject constructor(
    private val personalQuestRepository: PersonalQuestRepository,
    private val sharedQuestDataRepository: SharedQuestDataRepository
) {
    suspend operator fun invoke(userID: String) = withContext(Dispatchers.IO) {
        flow {
            emit(Resource.Loading)
            val response = personalQuestRepository.startPersonalQuest(userID)
            if (response is Resource.Success) {
                sharedQuestDataRepository.quest = response.result
                emit(Resource.Success(true))
            } else if (response is Resource.Error) {
                response.exception.printStackTrace()
                emit(Resource.Error(response.exception))
            }
        }
    }
}