package it.unipd.milan.padovaquest.shared_quests.domain.use_case

import android.location.Location
import it.unipd.milan.padovaquest.shared_quests.domain.model.Place
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest
import it.unipd.milan.padovaquest.shared_quests.domain.repo.PlacesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetNearestPlaceUseCase @Inject constructor(
    private val placesRepository: PlacesRepository
) {
    suspend operator fun invoke(location: Location, quest: Quest?): Place? =
        withContext(Dispatchers.IO) {
            return@withContext try {
                placesRepository.getNearPlace(location, quest)
            } catch (e: Exception) {
                null
            }

        }
}