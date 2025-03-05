package it.unipd.milan.padovaquest.shared_quests.domain.repo

import it.unipd.milan.padovaquest.shared_quests.domain.model.LocationStatus
import kotlinx.coroutines.flow.Flow

interface LocationClient {
    fun startLocationUpdates(interval: Long)
    fun getLocationUpdates(): Flow<LocationStatus>

    fun getLastLocationStatus(): LocationStatus
    fun emitAgain()

    fun stop()


}