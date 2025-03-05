package it.unipd.milan.padovaquest.feature_authentication.domain.use_case

data class AuthUseCases(
    val emailLoginUseCase: EmailLoginUseCase,
    val registerUseCase: RegisterUseCase,
    val signInWithGoogleUseCase: SignInWithGoogleUseCase
)
