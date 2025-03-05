package it.unipd.milan.padovaquest.feature_authentication.presentation.user_login

data class LoginState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
)
