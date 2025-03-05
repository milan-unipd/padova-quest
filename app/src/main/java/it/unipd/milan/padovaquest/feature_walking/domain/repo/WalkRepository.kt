package it.unipd.milan.padovaquest.feature_walking.domain.repo

import com.google.android.gms.maps.model.LatLngBounds
import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.shared_quests.domain.model.Place

interface WalkRepository {

    suspend fun getPlacesWithinBounds(mapBounds: LatLngBounds): List<Place>

    suspend fun setPlaceSeen(placeId: String)

    suspend fun getPlaceDescription(placeId: String): Resource<String>

}