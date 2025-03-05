package it.unipd.milan.padovaquest.feature_profile.presentation.my_quests.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.feature_profile.domain.GroupQuestResult
import it.unipd.milan.padovaquest.feature_profile.domain.use_cases.MyProfileUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupQuestResultsViewModel @Inject constructor(
    private val myProfileUseCases: MyProfileUseCases
) : ViewModel() {

    private val _questResultsFlow = MutableStateFlow<Resource<GroupQuestResult>?>(null)
    val questResultsFlow = _questResultsFlow.asStateFlow()

    fun getQuestResults(questID: String) {
        viewModelScope.launch {
            _questResultsFlow.value = myProfileUseCases.getGroupQuestResultsUseCase(questID)
        }
    }
}