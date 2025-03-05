package it.unipd.milan.padovaquest.shared_quests.domain.model

import android.location.Location

data class LocationStatus(
    val location: Location?,
    val isLocationEnabled: Boolean
)

