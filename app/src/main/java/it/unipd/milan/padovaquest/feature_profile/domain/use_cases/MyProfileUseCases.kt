package it.unipd.milan.padovaquest.feature_profile.domain.use_cases

data class MyProfileUseCases(
    val getQuestsUseCase: GetQuestsUseCase,
    val getGroupQuestResultsUseCase: GetGroupQuestResultsUseCase,
    val updateNameUseCase: UpdateNameUseCase
)
