package it.unipd.milan.padovaquest.feature_authentication.data

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.feature_authentication.domain.repo.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {


    override suspend fun logInWithEmail(email: String, password: String): Resource<Unit> {

        try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await().user
                ?: throw Exception("Error: User not found after email login")

            return Resource.Success(Unit)

        } catch (e: Exception) {
            return Resource.Error(e)
        }
    }

    override suspend fun registerWithEmail(name: String, email: String, password: String): Resource<Unit> {
        return try {
            val user = firebaseAuth.createUserWithEmailAndPassword(email, password).await().user
                ?: throw Exception("Error: User not found after registration")
            user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build()).await()
            firestore.collection("users").document(user.uid).set(mapOf("name" to name, "currentQuestID" to null)).await()
            firestore.collection("users").document(user.uid).collection("seenPlaces").document("placeholder").set(mapOf("x" to "x")).await()
            firestore.collection("users").document(user.uid).collection("seenPlaces").document("placeholder").delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e)
        }
    }

    override suspend fun registerWithGoogle(authCredential: AuthCredential): Resource<Unit> {
        return try {
            val user = firebaseAuth.signInWithCredential(authCredential).await().user
                ?: throw Exception("Error: User not found after google login")
            firestore.collection("users").document(user.uid).set(mapOf("name" to user.displayName, "currentQuestID" to null)).await()
            firestore.collection("users").document(user.uid).collection("seenPlaces").document("placeholder").set(mapOf("x" to "x")).await()
            firestore.collection("users").document(user.uid).collection("seenPlaces").document("placeholder").delete().await()
            return Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }

    }
}