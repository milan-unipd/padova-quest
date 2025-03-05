package it.unipd.milan.padovaquest.core.presentation

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BaseViewModel : ViewModel() {

    private val _toolbarState = MutableStateFlow(false)
    val toolbarState = _toolbarState.asStateFlow()


    fun showToolbar() {
        _toolbarState.value = true
    }

    fun hideToolbar() {
        _toolbarState.value = false
    }

    fun logOut() {
        Firebase.auth.signOut()
        _toolbarState.value = false
    }
}