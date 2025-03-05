package it.unipd.milan.padovaquest.shared_quests.domain.model

data class Question(
    var id: String? = null,
    val question: String = "",
    val answers: List<String> = emptyList()
)