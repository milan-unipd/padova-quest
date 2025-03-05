package it.unipd.milan.padovaquest.feature_profile.domain

data class GroupQuestResult(
    val scoreBoard: List<String>,
    val createdOn: String,
    val places: List<String>
)
