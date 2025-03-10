package it.unipd.milan.padovaquest.feature_group_quest.data

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import com.google.firebase.ktx.Firebase
import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.feature_group_quest.domain.repo.GroupQuestRepository
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest
import it.unipd.milan.padovaquest.shared_quests.domain.repo.QuestRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class GroupQuestRepoImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val questRepository: QuestRepository
) : GroupQuestRepository {

    private var snapshotListener: ListenerRegistration? = null

    override suspend fun createGroupQuest(userID: String): Resource<Quest> {
        return try {

            var placesAndQuestions = questRepository.getPlacesAndQuestions()
            if (placesAndQuestions is Resource.Error) throw placesAndQuestions.exception
            placesAndQuestions = placesAndQuestions as Resource.Success

            val quest = Quest(
                type = "group",
                status = "created",
                createdBy = userID,
                createdOn = Date(),
                places = placesAndQuestions.result.first,
                questions = placesAndQuestions.result.second,
                users = listOf(userID)
            )

            val batch = firestore.batch()

            val questRef = firestore.collection("quests").document()
            batch.set(questRef, quest.getGroupQuestMap())

            val participantRef = questRef.collection("participants").document(userID)
            batch.set(participantRef, mapOf("answers" to emptyMap<String, Map<String, Int>>(), "finishedOn" to null, "numOfCorrectAnswers" to 0, "name" to Firebase.auth.currentUser!!.displayName))
            batch.update(firestore.collection("users").document(userID), "currentQuestID", questRef.id)

            batch.commit().await()
            quest.id = questRef.id
            Resource.Success(quest)

        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun startGroupQuest(questID: String): Resource<Unit> {
        return try {
            firestore.collection("quests").document(questID).update("status", "started").await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun deleteGroupQuest(userID: String, questID: String): Resource<Unit> {
        return try {
            val batch = firestore.batch()
            val participantsRef = firestore.collection("quests").document(questID).collection("participants").get().await()
            participantsRef.forEach {
                batch.delete(it.reference)
            }
            batch.delete(firestore.collection("quests").document(questID))
            batch.update(firestore.collection("users").document(userID), "currentQuestID", null)
            batch.commit().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun cancelGroupQuest(userID: String, questID: String): Resource<Unit> {
        return try {
            val batch = firestore.batch()
            val participantRef = firestore.collection("quests").document(questID).collection("participants").document(userID)

            batch.update(participantRef, "finishedOn", FieldValue.serverTimestamp())
            val userRef = firestore.collection("users").document(Firebase.auth.currentUser!!.uid)
            batch.update(userRef, "currentQuestID", null)

            batch.commit().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun joinGroupQuest(
        userID: String,
        questID: String,
        onQuestStartedAction: () -> Unit,
        onQuestDeleteAction: () -> Unit,
        onErrorAction: (Exception) -> Unit
    ): Resource<Quest?> {
        return try {

            val questSnapshot = firestore.collection("quests").document(questID).get(Source.SERVER).await()
            if (!questSnapshot.exists()) {
                throw Exception("Quest not found")
            }

            val canJoin = questSnapshot.getString("status") == "created"
            if (!canJoin) {
                throw Exception("Can't join a quest that's already started!")
            }


            val batch = firestore.batch()
            val participantRef = firestore.collection("quests").document(questID).collection("participants").document(userID)

            batch.set(participantRef, mapOf("answers" to emptyMap<String, Map<String, Int>>(), "finishedOn" to null, "numOfCorrectAnswers" to 0, "name" to Firebase.auth.currentUser!!.displayName))
            batch.update(firestore.collection("quests").document(questID), "users", FieldValue.arrayUnion(userID))
            batch.update(firestore.collection("users").document(userID), "currentQuestID", questID)

            batch.commit().await()

            val questResource = questRepository.getCurrentQuest(userID)
            if (questResource is Resource.Error)
                throw questResource.exception
            val quest = (questResource as Resource.Success).result


            CoroutineScope(Dispatchers.IO).launch {
                callbackFlow {
                    snapshotListener = firestore.collection("quests").document(questID)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                close(error)
                                onErrorAction(error)
                                return@addSnapshotListener
                            }

                            if (snapshot?.exists() == true) {
                                trySend(snapshot.getString("status"))
                            } else {
                                trySend("deleted")
                                close() // Quest deleted, stop flow
                            }
                        }

                    awaitClose {
                        snapshotListener?.remove()
                    } // Remove listener when done
                }.collect { status ->
                    if (status is String) {
                        if (status == "started") {
                            onQuestStartedAction()
                        } else if (status == "deleted") {
                            onQuestDeleteAction()
                        }
                    }
                }
            }

            Resource.Success(quest)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun leaveGroupQuest(userID: String, questID: String): Resource<Unit> {
        return try {
            val batch = firestore.batch()
            val participantRef = firestore.collection("quests").document(questID).collection("participants").document(userID)

            batch.delete(participantRef)
            batch.update(firestore.collection("quests").document(questID), "users", FieldValue.arrayRemove(userID))
            batch.update(firestore.collection("users").document(userID), "currentQuestID", null)

            batch.commit().await()
            snapshotListener?.remove()
            Resource.Success(Unit)

        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                snapshotListener?.remove()
                return Resource.Success(Unit)
            }
            Resource.Error(e)
        }
    }
}