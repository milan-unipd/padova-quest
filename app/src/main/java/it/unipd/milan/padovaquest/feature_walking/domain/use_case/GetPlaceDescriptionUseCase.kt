package it.unipd.milan.padovaquest.feature_walking.domain.use_case

import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.feature_walking.domain.repo.WalkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetPlaceDescriptionUseCase @Inject constructor(
    private val walkRepository: WalkRepository
) {
    suspend operator fun invoke(placeId: String) = withContext(Dispatchers.IO) {
        flow {
            emit(Resource.Loading)
            try {
                emit(walkRepository.getPlaceDescription(placeId))
            } catch (e: Exception) {
                emit(Resource.Error(e))
            }
        }
    }
}