package it.unipd.milan.padovaquest.shared_quests.presentation.service

import it.unipd.milan.padovaquest.feature_group_quest.domain.model.GroupQuestStatus
import it.unipd.milan.padovaquest.shared_quests.domain.model.Place
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest
import it.unipd.milan.padovaquest.shared_quests.domain.repo.LocationClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedQuestDataRepository @Inject constructor(
    private val locationClient: LocationClient,
) {

    //region Location
    val locationFlow = locationClient.getLocationUpdates()
    fun getLastLocationStatus() = locationClient.getLastLocationStatus()

    fun startLocationUpdates(locationInterval: Long) {
        locationClient.startLocationUpdates(locationInterval)
    }

    fun emitLastLocationAgain() {
        locationClient.emitAgain()
    }

    fun stop() {
        locationClient.stop()
    }


    //endregion

    //region Place Proximity Detection

    private val _nearestPlaceFlow = MutableSharedFlow<Place?>(replay = 1)
    val nearestPlaceFlow = _nearestPlaceFlow.asSharedFlow()

    suspend fun emmitNearestPlaceFlow(place: Place?) {
        _nearestPlaceFlow.emit(place)
    }


    //endregion

    //region Quest
    var quest: Quest? = null

    private val _groupQuestStatus = MutableStateFlow(GroupQuestStatus())
    val groupQuestStatus = _groupQuestStatus.asStateFlow()

    fun setGroupQuestStatus(groupQuestStatus: GroupQuestStatus) {
        _groupQuestStatus.value = groupQuestStatus
    }


    //endregion
}