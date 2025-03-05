package it.unipd.milan.padovaquest.feature_walking.domain.use_case

import it.unipd.milan.padovaquest.feature_walking.domain.repo.WalkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetPlaceSeenUseCase @Inject constructor(
    private val walkRepository: WalkRepository
) {
    suspend operator fun invoke(placeId: String) = withContext(Dispatchers.IO) {
        walkRepository.setPlaceSeen(placeId)
    }
}