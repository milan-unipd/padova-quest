package it.unipd.milan.padovaquest.feature_authentication.di

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.unipd.milan.padovaquest.feature_authentication.data.AuthRepositoryImpl
import it.unipd.milan.padovaquest.feature_authentication.domain.repo.AuthRepository
import it.unipd.milan.padovaquest.feature_authentication.domain.use_case.AuthUseCases
import it.unipd.milan.padovaquest.feature_authentication.domain.use_case.EmailLoginUseCase
import it.unipd.milan.padovaquest.feature_authentication.domain.use_case.RegisterUseCase
import it.unipd.milan.padovaquest.feature_authentication.domain.use_case.SignInWithGoogleUseCase
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository(firestore: FirebaseFirestore): AuthRepository {
        return AuthRepositoryImpl(Firebase.auth, firestore)
    }

    @Provides
    @Singleton
    fun provideAuthUseCases(repo: AuthRepository): AuthUseCases {
        return AuthUseCases(
            emailLoginUseCase = EmailLoginUseCase(repo),
            registerUseCase = RegisterUseCase(repo),
            signInWithGoogleUseCase = SignInWithGoogleUseCase(repo)
        )
    }
}