package it.unipd.milan.padovaquest.shared_quests.domain.model

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Place(
    var id: String = "",
    val name: String = "",
    var visited: Boolean = false,
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val geohash: String = "",
)
