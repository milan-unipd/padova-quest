package it.unipd.milan.padovaquest.feature_profile.presentation.my_quests.personal

import dagger.hilt.android.lifecycle.HiltViewModel
import it.unipd.milan.padovaquest.feature_profile.domain.use_cases.GetQuestsUseCase
import it.unipd.milan.padovaquest.feature_profile.presentation.my_quests.MyQuestsViewModel
import javax.inject.Inject

@HiltViewModel
class MyPersonalQuestsViewModel @Inject constructor(getQuestsUseCase: GetQuestsUseCase) : MyQuestsViewModel(getQuestsUseCase) {}