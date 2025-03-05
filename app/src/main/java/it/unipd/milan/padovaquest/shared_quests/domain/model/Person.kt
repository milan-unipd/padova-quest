package it.unipd.milan.padovaquest.shared_quests.domain.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Person(
    var id: String? = null,
    val currentQuestID: String? = null,
    val name: String? = null,
    val numOfCorrectAnswers: Int? = null,
)
