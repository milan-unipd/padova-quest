package it.unipd.milan.padovaquest.feature_walking.domain.use_case

import com.google.android.gms.maps.model.LatLngBounds
import it.unipd.milan.padovaquest.feature_walking.domain.repo.WalkRepository
import it.unipd.milan.padovaquest.shared_quests.domain.model.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetPlacesWithinBoundsUseCase @Inject constructor(
    private val walkRepository: WalkRepository
) {
    suspend operator fun invoke(mapBounds: LatLngBounds): List<Place> = withContext(Dispatchers.IO){
        return@withContext walkRepository.getPlacesWithinBounds(mapBounds)
    }
}