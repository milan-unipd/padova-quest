package it.unipd.milan.padovaquest.feature_profile.domain.use_cases

import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.feature_profile.domain.repo.MyProfileRepository
import javax.inject.Inject

class UpdateNameUseCase @Inject constructor(
    private val myProfileRepository: MyProfileRepository
) {

    suspend operator fun invoke(userID: String, newName: String): Resource<Unit> {
        return myProfileRepository.updateUserName(userID, newName)

    }
}