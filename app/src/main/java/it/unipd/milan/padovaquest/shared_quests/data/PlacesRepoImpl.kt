package it.unipd.milan.padovaquest.shared_quests.data

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.ktx.Firebase
import it.unipd.milan.padovaquest.feature_walking.domain.repo.WalkRepository
import it.unipd.milan.padovaquest.shared_quests.domain.model.Place
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest
import it.unipd.milan.padovaquest.shared_quests.domain.repo.PlacesRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.math.cos

class PlacesRepoImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val walkRepository: WalkRepository
) : PlacesRepository {

    private val DISTANCE = 10.0


    override suspend fun getNearPlace(location: Location, quest: Quest?): Place? {


        val nearestPlace = if (quest?.id == null) {
            val nearPlaces = walkRepository.getPlacesWithinBounds(getLatLngBounds(location))
            val seenPlacesSnapshot = firestore
                .collection("users").document(Firebase.auth.currentUser!!.uid)
                .collection("seenPlaces")
                .get(Source.SERVER)
                .await()

            val seenPlaces = seenPlacesSnapshot.documents.mapNotNull { it["id"] as? String }.toSet()

            nearPlaces
                .filterNot { place ->
                    seenPlaces.contains(place.id)
                }.minByOrNull {
                    val placeLoc = Location("place").apply {
                        latitude = it.location.latitude
                        longitude = it.location.longitude
                    }
                    location.distanceTo(placeLoc)
                }
        } else {
            quest.places
                .filterNot { it.visited }
                .mapNotNull { place ->
                    val placeLoc = Location("place").apply {
                        latitude = place.location.latitude
                        longitude = place.location.longitude
                    }
                    val distance = location.distanceTo(placeLoc)
                    place.takeIf { distance <= DISTANCE }?.let { it to distance }
                }.minByOrNull { it.second }
                ?.first
        }
        return nearestPlace
    }

    private fun getLatLngBounds(center: Location, radiusMeters: Double = DISTANCE): LatLngBounds {
        val latOffset = radiusMeters / 111320.0
        val lngOffset = radiusMeters / (111320.0 * cos(Math.toRadians(center.latitude)))

        val southwest = LatLng(center.latitude - latOffset, center.longitude - lngOffset)
        val northeast = LatLng(center.latitude + latOffset, center.longitude + lngOffset)

        return LatLngBounds(southwest, northeast)
    }

}