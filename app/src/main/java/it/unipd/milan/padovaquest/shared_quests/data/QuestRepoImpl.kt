package it.unipd.milan.padovaquest.shared_quests.data

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.ktx.Firebase
import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.shared_quests.domain.model.Place
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest
import it.unipd.milan.padovaquest.shared_quests.domain.model.Question
import it.unipd.milan.padovaquest.shared_quests.domain.repo.QuestRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class QuestRepoImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : QuestRepository {

    override suspend fun getCurrentQuest(userID: String): Resource<Quest?> {
        return try {

            val questID = firestore
                .collection("users")
                .document(userID)
                .get(Source.SERVER)
                .await()
                .get("currentQuestID") as? String

            if (questID == null)
                return Resource.Success(Quest(id = null))

            val questSnapshot = firestore.collection("quests")
                .document(questID)
                .get(Source.SERVER)
                .await()

            if (!questSnapshot.exists()) {
                firestore.collection("users").document(userID).update("currentQuestID", null).await()
                return Resource.Success(null)
            }

            val quest = questSnapshot.toObject(Quest::class.java)!!.apply { id = questID }

            if (quest.type == "personal") {

                quest.places.forEach { place ->
                    place.visited = quest.answers.containsKey(place.id)
                }
            } else {

                val participantSnapshot = firestore.collection("quests")
                    .document(questID)
                    .collection("participants")
                    .document(userID)
                    .get(Source.SERVER)
                    .await()

                quest.numOfCorrectAnswers = (participantSnapshot.get("numOfCorrectAnswers") as Long).toInt()
                quest.finishedOn = participantSnapshot.getDate("finishedOn")
                val answers = (participantSnapshot.get("answers") as Map<*, *>)
                    .mapKeys { it.key as String }
                    .mapValues {
                        (it.value as Map<*, *>).mapKeys { it.key as String }.mapValues { it.value as Int }
                    }
                    .toMutableMap()
                quest.answers = answers


            }
            Resource.Success(quest)

        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e)
        }
    }

    override suspend fun getPlacesAndQuestions(): Resource<Pair<List<Place>, Map<String, Question>>> {
        return try {
            val places = firestore.collection("places")
                .get(Source.SERVER)
                .await()
                .documents
                .shuffled()
                .take(1) // TODO!!!
                .map { it.toObject(Place::class.java)!!.apply { id = it.id } }

            val questions = mutableMapOf<String, Question>()
            places.forEach { place ->
                val question = firestore.collection("places")
                    .document(place.id)
                    .collection("questions")
                    .get()
                    .await()
                    .documents
                    .shuffled()
                    .first()
                questions[place.id] = question.toObject(Question::class.java)!!.apply { id = question.id }
            }
            Resource.Success(Pair(places, questions))
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }


    override suspend fun answerQuestion(userID: String, quest: Quest, placeID: String, questionID: String, answerIndex: Int, isLastQuestion: Boolean): Resource<Unit> {
        return try {

            val batch = firestore.batch()
            val questRef = firestore.collection("quests").document(quest.id!!)
            if (quest.type == "personal")
                answerPersonalQuestQuestion(batch, questRef, placeID, questionID, answerIndex, quest.numOfCorrectAnswers, isLastQuestion)
            else if (quest.type == "group")
                answerGroupQuestQuestion(batch, userID, quest.id!!, placeID, questionID, answerIndex, quest.numOfCorrectAnswers, isLastQuestion)
            batch.commit().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }


    }

    private fun answerPersonalQuestQuestion(batch: WriteBatch, questRef: DocumentReference, placeID: String, questionID: String, answerIndex: Int, numOfCorrectAnswers: Int, isLastQuestion: Boolean) {
        batch.update(questRef, "answers.$placeID.$questionID", answerIndex)
        if (answerIndex == 0)
            batch.update(questRef, "numOfCorrectAnswers", numOfCorrectAnswers + 1)
        if (isLastQuestion) {
            batch.update(questRef, "finishedOn", FieldValue.serverTimestamp())
            val userRef = firestore.collection("users").document(Firebase.auth.currentUser!!.uid)
            batch.update(userRef, "currentQuestID", null)
        }
    }

    private fun answerGroupQuestQuestion(batch: WriteBatch, userID: String, questID: String, placeID: String, questionID: String, answerIndex: Int, numOfCorrectAnswers: Int, isLastQuestion: Boolean) {
        val participantRef = firestore.collection("quests").document(questID).collection("participants").document(userID)

        batch.update(participantRef, "answers.$placeID.$questionID", answerIndex)

        if (answerIndex == 0)
            batch.update(participantRef, "numOfCorrectAnswers", numOfCorrectAnswers + 1)

        if (isLastQuestion) {
            batch.update(participantRef, "finishedOn", FieldValue.serverTimestamp())
            val userRef = firestore.collection("users").document(Firebase.auth.currentUser!!.uid)
            batch.update(userRef, "currentQuestID", null)
        }
    }

}