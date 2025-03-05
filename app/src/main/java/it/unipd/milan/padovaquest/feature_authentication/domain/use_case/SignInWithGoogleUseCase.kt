package it.unipd.milan.padovaquest.feature_authentication.domain.use_case

import com.google.firebase.auth.AuthCredential
import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.feature_authentication.domain.repo.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SignInWithGoogleUseCase  @Inject constructor(
    private val repository: AuthRepository
) {

    suspend operator fun invoke(authCredential: AuthCredential) =
        withContext(Dispatchers.IO) {
            flow {
                emit(Resource.Loading)
                emit(repository.registerWithGoogle(authCredential))
            }
        }
}