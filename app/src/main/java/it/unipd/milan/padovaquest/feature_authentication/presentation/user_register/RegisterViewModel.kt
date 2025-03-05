package it.unipd.milan.padovaquest.feature_authentication.presentation.user_register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.feature_authentication.domain.use_case.AuthUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
) : ViewModel() {

    private val _registerFlow = MutableStateFlow<Resource<Unit>?>(null)
    val registerFlow = _registerFlow.asStateFlow()

    fun emptyRegisterFlow() {
        _registerFlow.value = null
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            authUseCases.registerUseCase(name, email, password).collect {
                _registerFlow.emit(it)
            }
        }
    }
}