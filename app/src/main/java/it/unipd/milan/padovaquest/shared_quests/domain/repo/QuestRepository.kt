package it.unipd.milan.padovaquest.shared_quests.domain.repo

import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.shared_quests.domain.model.Place
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest
import it.unipd.milan.padovaquest.shared_quests.domain.model.Question

interface QuestRepository {

    suspend fun getCurrentQuest(userID: String): Resource<Quest?>

    suspend fun getPlacesAndQuestions(): Resource<Pair<List<Place>, Map<String, Question>>>

    suspend fun answerQuestion(userID: String, quest: Quest, placeID: String, questionID: String, answerIndex: Int, isLastQuestion: Boolean): Resource<Unit>

}