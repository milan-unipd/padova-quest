package it.unipd.milan.padovaquest.feature_profile.presentation.my_quests.group

import dagger.hilt.android.lifecycle.HiltViewModel
import it.unipd.milan.padovaquest.feature_profile.domain.use_cases.GetQuestsUseCase
import it.unipd.milan.padovaquest.feature_profile.presentation.my_quests.MyQuestsViewModel
import javax.inject.Inject

@HiltViewModel
class MyGroupQuestsViewModel @Inject constructor(getQuestsUseCase: GetQuestsUseCase) : MyQuestsViewModel(getQuestsUseCase)