package it.unipd.milan.padovaquest.shared_quests.domain.model

import com.google.firebase.firestore.Exclude
import java.util.Date

data class Quest(
    var id: String? = null,
    val type: String = "",
    val createdBy: String? = null,
    var creatorName: String? = null,
    val createdOn: Date? = null,
    var finishedOn: Date? = null,
    val places: List<Place> = emptyList(),
    val questions: Map<String, Question> = emptyMap(),
    var answers: MutableMap<String, Map<String, Int>> = mutableMapOf(),
    var numOfCorrectAnswers: Int = 0,
    var status: String = "",
    var users: List<String>? = null
) {
    @Exclude
    fun getPersonalQuestMap(): Map<String, Any?> {
        return mapOf(
            "type" to type,
            "createdBy" to createdBy,
            "createdOn" to createdOn,
            "finishedOn" to finishedOn,
            "places" to places,
            "questions" to questions,
            "answers" to answers,
            "numOfCorrectAnswers" to numOfCorrectAnswers,
        )
    }

    @Exclude
    fun getGroupQuestMap(): Map<String, Any?> {
        return mapOf(
            "type" to type,
            "createdBy" to createdBy,
            "createdOn" to createdOn,
            "places" to places,
            "questions" to questions,
            "status" to status,
            "users" to users
        )
    }
}





