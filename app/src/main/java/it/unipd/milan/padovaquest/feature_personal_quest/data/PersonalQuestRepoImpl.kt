package it.unipd.milan.padovaquest.feature_personal_quest.data

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.feature_personal_quest.domain.repo.PersonalQuestRepository
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest
import it.unipd.milan.padovaquest.shared_quests.domain.repo.QuestRepository
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class PersonalQuestRepoImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val questRepository: QuestRepository
) : PersonalQuestRepository {

    override suspend fun startPersonalQuest(userID: String): Resource<Quest> {
        return try {

            var placesAndQuestions = questRepository.getPlacesAndQuestions()
            if (placesAndQuestions is Resource.Error)
                throw placesAndQuestions.exception
            placesAndQuestions = placesAndQuestions as Resource.Success

            val quest = Quest(
                type = "personal",
                createdBy = userID,
                createdOn = Date(),
                places = placesAndQuestions.result.first,
                questions = placesAndQuestions.result.second,
                numOfCorrectAnswers = 0)

            val questRef = firestore.collection("quests").add(quest.getPersonalQuestMap()).await()
            quest.id = questRef.id
            firestore.collection("users").document(userID).update("currentQuestID", questRef.id).await()

            Resource.Success(quest)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun cancelPersonalQuest(quest: Quest): Resource<Unit> {
        return try {
            val batch = firestore.batch()
            val questRef = firestore.collection("quests").document(quest.id!!)
            batch.update(questRef, "finishedOn", FieldValue.serverTimestamp())
            batch.update(questRef, "isCanceled", true)

            val userRef = firestore.collection("users").document(Firebase.auth.currentUser!!.uid)
            batch.update(userRef, "currentQuestID", null)

            batch.commit().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e)
        }
    }
}