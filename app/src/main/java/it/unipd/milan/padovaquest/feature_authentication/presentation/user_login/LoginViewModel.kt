package it.unipd.milan.padovaquest.feature_authentication.presentation.user_login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.feature_authentication.domain.use_case.AuthUseCases
import it.unipd.milan.padovaquest.shared_quests.domain.use_case.QuestUseCases
import it.unipd.milan.padovaquest.shared_quests.presentation.service.SharedQuestDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val questsUseCases: QuestUseCases,
    internal val sharedQuestDataRepository: SharedQuestDataRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginState())
    val loginState = _loginState.asStateFlow()


    fun doLogin(username: String, password: String) {
        viewModelScope.launch {
            authUseCases.emailLoginUseCase(username, password).onEach { result ->
                when (result) {
                    is Resource.Error -> _loginState.emit(LoginState(error = result.exception.localizedMessage
                        ?: "An unexpected error occurred"))

                    Resource.Loading -> _loginState.emit(LoginState(isLoading = true))
                    is Resource.Success -> _loginState.emit(LoginState(success = true))
                }
            }.launchIn(viewModelScope)
        }
    }

    suspend fun getCurrentQuest(userID: String) =
        questsUseCases.getCurrentQuestUseCase(userID)

    fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        viewModelScope.launch {
            authUseCases.signInWithGoogleUseCase(credential).collect { result ->
                when (result) {
                    is Resource.Error -> _loginState.emit(LoginState(error = result.exception.localizedMessage
                        ?: "An unexpected error occurred"))

                    Resource.Loading -> _loginState.emit(LoginState(isLoading = true))
                    is Resource.Success -> _loginState.emit(LoginState(success = true))
                }

            }
        }

    }

}