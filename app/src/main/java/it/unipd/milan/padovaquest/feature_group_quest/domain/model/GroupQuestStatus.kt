package it.unipd.milan.padovaquest.feature_group_quest.domain.model

data class GroupQuestStatus(
    val shouldCreate: Boolean = false,
    val isBeingCreated: Boolean = false,
    val hasBeenCreated: Boolean = false,
    val isBeingDeleted: Boolean = false,
    val wasDeleted: Boolean = false,
    val isStarting: Boolean = false,
    val hasStarted: Boolean = false,
    val shouldJoin: Boolean = false,
    val isJoining: Boolean = false,
    val isWaitingToStart: Boolean = false,
    val isLeaving: Boolean = false,
    val hasLeft:Boolean = false,
    val exception: Exception? = null
)
