package it.unipd.milan.padovaquest.feature_profile.presentation.my_quests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.feature_profile.domain.use_cases.GetQuestsUseCase
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


open class MyQuestsViewModel(
    private val getQuestsUseCase: GetQuestsUseCase
) : ViewModel() {

    private val _scrollState = MutableStateFlow(0)
    val scrollState = _scrollState.asStateFlow()

    private val _questsFlow = MutableStateFlow<Resource<List<Quest>>>(Resource.Success(emptyList()))
    val questsFlow = _questsFlow.asStateFlow()


    fun onScrollChanged(newScrollValue: Int) {
        _scrollState.value = newScrollValue
    }

    fun getQuests(startIndex: Int, windowSize: Int, type: String) {
        viewModelScope.launch {
            val result = getQuestsUseCase(Firebase.auth.currentUser!!.uid, type, startIndex, windowSize)
            _questsFlow.value = result
        }
    }

}