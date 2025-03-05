package it.unipd.milan.padovaquest.feature_authentication.domain.repo

import com.google.firebase.auth.AuthCredential
import it.unipd.milan.padovaquest.core.util.Resource

interface AuthRepository {

    suspend fun logInWithEmail(email: String, password:String): Resource<Unit>
    suspend fun registerWithEmail(name: String, email: String, password: String): Resource<Unit>
    suspend fun registerWithGoogle(authCredential: AuthCredential): Resource<Unit>
}