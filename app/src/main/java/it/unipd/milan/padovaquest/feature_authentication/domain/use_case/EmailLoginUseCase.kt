package it.unipd.milan.padovaquest.feature_authentication.domain.use_case

import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.feature_authentication.domain.repo.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EmailLoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {

    suspend operator fun invoke(username: String, password: String) = withContext(Dispatchers.IO) {
        flow {
            emit(Resource.Loading)
            try {
                emit(repository.logInWithEmail(email = username, password = password))
            } catch (e: Exception) {
                emit(Resource.Error(e))
            }

        }
    }
}