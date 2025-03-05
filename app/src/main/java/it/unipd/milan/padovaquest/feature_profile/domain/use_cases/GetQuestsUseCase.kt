package it.unipd.milan.padovaquest.feature_profile.domain.use_cases

import it.unipd.milan.padovaquest.feature_profile.domain.repo.MyProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetQuestsUseCase @Inject constructor(
    private val myProfileRepository: MyProfileRepository
) {

    suspend operator fun invoke(userID: String, type: String, startIndex: Int, pageSize: Int) =
        withContext(Dispatchers.IO) {
            return@withContext myProfileRepository.getQuests(userID, type, startIndex, pageSize)
        }
}