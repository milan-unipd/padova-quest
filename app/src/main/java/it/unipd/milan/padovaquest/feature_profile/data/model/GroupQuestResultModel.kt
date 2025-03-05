package it.unipd.milan.padovaquest.feature_profile.data.model

import it.unipd.milan.padovaquest.shared_quests.domain.model.Person
import java.util.Date

data class GroupQuestResultModel(
    val places: List<String>,
    val createdOn: Date,
    val users: List<Person>,
    val finishTimes: Map<String, Date?>
)