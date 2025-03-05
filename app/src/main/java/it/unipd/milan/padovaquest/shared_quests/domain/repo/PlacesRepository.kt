package it.unipd.milan.padovaquest.shared_quests.domain.repo

import android.location.Location
import it.unipd.milan.padovaquest.shared_quests.domain.model.Place
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest

interface PlacesRepository {

    suspend fun getNearPlace(location: Location, quest: Quest?): Place?

}