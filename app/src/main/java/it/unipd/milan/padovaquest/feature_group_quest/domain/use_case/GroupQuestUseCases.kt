package it.unipd.milan.padovaquest.feature_group_quest.domain.use_case

data class GroupQuestUseCases(
    val createGroupQuestUseCase: CreateGroupQuestUseCase,
    val startGroupQuestUseCase: StartGroupQuestUseCase,
    val deleteGroupQuestUseCase: DeleteGroupQuestUseCase,
    val cancelQuestUseCase: CancelQuestUseCase,
    val joinGroupQuestUseCase: JoinGroupQuestUseCase,
    val leaveGroupQuestUseCase: LeaveGroupQuestUseCase
)