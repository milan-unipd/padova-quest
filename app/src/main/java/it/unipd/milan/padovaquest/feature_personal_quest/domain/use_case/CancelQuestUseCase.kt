package it.unipd.milan.padovaquest.feature_personal_quest.domain.use_case

import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.feature_personal_quest.domain.repo.PersonalQuestRepository
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest
import it.unipd.milan.padovaquest.shared_quests.presentation.service.SharedQuestDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CancelQuestUseCase @Inject constructor(
    private val questRepository: PersonalQuestRepository,
    private val sharedQuestDataRepository: SharedQuestDataRepository
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        flow {
            emit(Resource.Loading)
            val quest = sharedQuestDataRepository.quest

            if (quest?.id == null)
                return@flow

            val response = questRepository.cancelPersonalQuest(quest)
            if (response is Resource.Success) {
                sharedQuestDataRepository.quest = Quest()
                emit(Resource.Success(Unit))
            } else if (response is Resource.Error) {
                emit(Resource.Error(response.exception))
            }
        }
    }
}