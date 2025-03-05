package it.unipd.milan.padovaquest.shared_quests.presentation.trivia_fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.shared_quests.domain.model.Place
import it.unipd.milan.padovaquest.shared_quests.domain.use_case.QuestUseCases
import it.unipd.milan.padovaquest.shared_quests.presentation.service.SharedQuestDataRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TriviaViewModel @Inject constructor(
    val sharedQuestDataRepository: SharedQuestDataRepository,
    val questUseCases: QuestUseCases
) : ViewModel() {

    lateinit var place: Place

    private val _answerFlow = MutableSharedFlow<Resource<Boolean>?>(replay = 1)
    val answerFlow = _answerFlow.asSharedFlow()

    fun answerQuestion(questionID: String, answerIndex: Int) {
        viewModelScope.launch {
            val placeID = place.id
            questUseCases.answerQuestionUseCase(Firebase.auth.currentUser!!.uid, placeID, questionID, answerIndex).onEach {
                _answerFlow.emit(it)
            }.launchIn(viewModelScope)
        }
    }

    fun emptyAnswerFlow() {
        _answerFlow.tryEmit(null)
    }

}