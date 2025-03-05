package it.unipd.milan.padovaquest.shared_quests.domain.use_case

import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.shared_quests.domain.repo.QuestRepository
import it.unipd.milan.padovaquest.shared_quests.presentation.service.SharedQuestDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

class AnswerQuestionUseCase @Inject constructor(
    private val questRepository: QuestRepository,
    private val sharedQuestDataRepository: SharedQuestDataRepository
) {
    suspend operator fun invoke(userID: String, placeID: String, questionID: String, answerIndex: Int) =
        withContext(Dispatchers.IO) {
            flow {
                emit(Resource.Loading)
                val quest = sharedQuestDataRepository.quest!!

                val result = questRepository.answerQuestion(userID, quest, placeID, questionID, answerIndex, quest.answers.size + 1 == quest.questions.size)
                if (result is Resource.Success) {

                    quest.places.find { it.id == placeID }!!.visited = true
                    if (answerIndex == 0)
                        quest.numOfCorrectAnswers += 1

                    quest.answers[placeID] = mapOf(questionID to answerIndex)

                    if (quest.answers.size == quest.questions.size) {
                        quest.finishedOn = Date()
                    }

                    emit(Resource.Success(answerIndex == 0))
                } else
                    emit(Resource.Error(Exception("There was an error answering the question")))
            }
        }
}