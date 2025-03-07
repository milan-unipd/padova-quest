package it.unipd.milan.padovaquest.feature_profile.data

import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.ktx.Firebase
import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.feature_profile.data.model.GroupQuestResultModel
import it.unipd.milan.padovaquest.feature_profile.domain.repo.MyProfileRepository
import it.unipd.milan.padovaquest.shared_quests.domain.model.Person
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class MyProfileRepoImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MyProfileRepository {

    override suspend fun getQuests(userID: String, type: String, startIndex: Int, pageSize: Int): Resource<List<Quest>> {
        return try {
            if (type == "personal")
                return getPersonalQuests(userID, startIndex, pageSize)
            else if (type == "group")
                return getGroupQuests(userID, startIndex, pageSize)
            return Resource.Success(emptyList())
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    private suspend fun getPersonalQuests(userID: String, startIndex: Int, pageSize: Int): Resource<List<Quest>> {
        return try {
            val questsSnapshot = firestore.collection("quests")
                .whereEqualTo("createdBy", userID)
                .whereEqualTo("type", "personal")
                .orderBy("createdOn", Query.Direction.DESCENDING)
                .limit((startIndex + pageSize).toLong())
                .get(Source.SERVER)
                .await()
            val quests = questsSnapshot.documents.subList(startIndex, minOf(startIndex + pageSize, questsSnapshot.size()))
                .map {
                    it.toObject(Quest::class.java)!!.apply { id = it.id }
                }
            Resource.Success(quests)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e)
        }

    }

    private suspend fun getGroupQuests(userID: String, startIndex: Int, pageSize: Int): Resource<List<Quest>> {
        return try {
            val questsSnapshot = firestore.collection("quests")
                .whereEqualTo("type", "group")
                .whereArrayContains("users", userID)
                .orderBy("createdOn", Query.Direction.DESCENDING)
                .limit((startIndex + pageSize).toLong())
                .get(Source.SERVER)
                .await()


            val quests = questsSnapshot.documents.subList(startIndex, minOf(startIndex + pageSize, questsSnapshot.size()))
                .map { questSnapshot ->

                    val quest = questSnapshot.toObject(Quest::class.java)!!.apply { id = questSnapshot.id }

                    val participantSnapshot = firestore.collection("quests")
                        .document(quest.id!!)
                        .collection("participants")
                        .document(userID)
                        .get(Source.SERVER)
                        .await()

                    quest.numOfCorrectAnswers = (participantSnapshot.get("numOfCorrectAnswers") as Long).toInt()
                    quest.finishedOn = participantSnapshot.getDate("finishedOn")
                    val answers = (participantSnapshot.get("answers") as Map<*, *>)
                        .mapKeys { it.key as String }
                        .mapValues {
                            (it.value as Map<*, *>).mapKeys { it.key as String }.mapValues { (it.value as Long).toInt() }
                        }
                        .toMutableMap()
                    quest.answers = answers

                    quest.creatorName = firestore.collection("users").document(quest.createdBy!!).get(Source.SERVER).await().get("name") as String

                    quest
                }

            return Resource.Success(quests)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e)
        }
    }

    override suspend fun getGroupQuestResults(questID: String): Resource<GroupQuestResultModel> {
        return try {
            val questSnapshot = firestore.collection("quests")
                .document(questID)
                .get(Source.SERVER)
                .await()

            val quest = questSnapshot.toObject(Quest::class.java)!!.apply { id = questID }
            val places = quest.places.map {
                it.name
            }

            val participantsSnapshot = firestore.collection("quests").document(questID).collection("participants").get(Source.SERVER).await()
            val users = mutableListOf<Person>()
            val finishTimes = mutableMapOf<String, Date?>()

            participantsSnapshot.documents.forEach { documentSnapshot ->

                val participant = documentSnapshot.toObject(Person::class.java)!!.apply { id = documentSnapshot.id }

                val participantSnapshot = firestore.collection("quests")
                    .document(questID)
                    .collection("participants")
                    .document(participant.id!!)
                    .get(Source.SERVER)
                    .await()

                val finishedOn = participantSnapshot.getDate("finishedOn")

                if (finishedOn != null) {
                    if ((participantSnapshot.get("answers") as Map<*, *>).entries.size == quest.questions.size) {
                        finishTimes[participant.id!!] = finishedOn
                    } else {
                        finishTimes[participant.id!!] = Date(0)
                    }
                } else {
                    finishTimes[participant.id!!] = null
                }

                users.add(participant)

            }

            Resource.Success(GroupQuestResultModel(places, questSnapshot.getDate("createdOn")!!, users, finishTimes))
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e)
        }
    }

    override suspend fun updateUserName(userID: String, newName: String): Resource<Unit> {
        return try {
            val user = Firebase.auth.currentUser ?: throw Exception("User not found")
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()
            user.updateProfile(profileUpdates).await()

            val batch = firestore.batch()

            val userDocRef = firestore.collection("users").document(userID)
            batch.update(userDocRef, "name", newName)

            val groupQuests = firestore.collection("quests")
                .whereEqualTo("type", "group")
                .whereArrayContains("users", userID)
                .get(Source.SERVER)
                .await()

            for (document in groupQuests.documents) {
                val participantDocRef = firestore.collection("quests")
                    .document(document.id)
                    .collection("participants")
                    .document(userID)
                batch.update(participantDocRef, "name", newName)
            }

            batch.commit().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }


    }
}