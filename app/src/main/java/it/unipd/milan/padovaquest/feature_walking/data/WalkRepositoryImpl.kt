package it.unipd.milan.padovaquest.feature_walking.data

import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.ktx.Firebase
import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.feature_walking.domain.repo.WalkRepository
import it.unipd.milan.padovaquest.shared_quests.domain.model.Place
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class WalkRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : WalkRepository {

    override suspend fun getPlacesWithinBounds(mapBounds: LatLngBounds): List<Place> {

        val northEast = GeoLocation(mapBounds.northeast.latitude, mapBounds.northeast.longitude)
        val southWest = GeoLocation(mapBounds.southwest.latitude, mapBounds.southwest.longitude)
        val centerLat = (northEast.latitude + southWest.latitude) / 2
        val centerLng = (northEast.longitude + southWest.longitude) / 2
        val center = GeoLocation(centerLat, centerLng)

        val radiusInM = GeoFireUtils.getDistanceBetween(northEast, southWest) * 500
        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)

        return try {
            val queries = bounds.map { bound ->
                firestore.collection("places")
                    .orderBy("geohash")
                    .startAt(bound.startHash)
                    .endAt(bound.endHash)
                    .get(Source.SERVER)
            }
            val placesQuerySnapshot = queries.map { it.await() }
            val results = mutableListOf<Place>()
            for (snap in placesQuerySnapshot) {
                for (document in snap.documents) {
                    val location = document.getGeoPoint("location") ?: continue
                    val data = document.toObject(Place::class.java)!!.apply { id = document.id }
                    if (mapBounds.contains(LatLng(location.latitude, location.longitude))) {
                        results.add(data)
                    }
                }
            }
            results

        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun setPlaceSeen(placeId: String) {
        firestore.collection("users")
            .document(Firebase.auth.currentUser!!.uid)
            .collection("seenPlaces")
            .add(mapOf("id" to placeId))
            .await()
    }

    override suspend fun getPlaceDescription(placeId: String): Resource<String> {
        return try {
            val place = firestore.collection("places")
                .document(placeId)
                .get(Source.SERVER)
                .await()
            val description = place.get("description") ?: return Resource.Error(Exception("Description not found"))
            return Resource.Success(description.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e)
        }
    }


}