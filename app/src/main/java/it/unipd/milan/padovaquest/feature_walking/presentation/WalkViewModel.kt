package it.unipd.milan.padovaquest.feature_walking.presentation

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unipd.milan.padovaquest.feature_personal_quest.domain.use_case.PersonalQuestUseCases
import it.unipd.milan.padovaquest.feature_walking.domain.use_case.WalkUseCases
import it.unipd.milan.padovaquest.shared_quests.domain.model.Place
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalkViewModel @Inject constructor(
    private val walkUseCases: WalkUseCases,
    private val personalQuestUseCases: PersonalQuestUseCases,
) : ViewModel() {

    private val _mapBounds = MutableStateFlow<LatLngBounds?>(null)

    private var places = emptyList<Place>()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val placesFlow = _mapBounds.filterNotNull().debounce(500)
        .flatMapLatest { bounds ->
            flow {
                places = walkUseCases.getPlacesWithinBoundsUseCase(bounds)
                emit(places)
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun getNearbyPlaces(bounds: LatLngBounds) {
        _mapBounds.value = bounds
    }

    fun getNearPlace(location: Location): Place? {
        var minDistance = Float.MAX_VALUE
        var nearestPlace: Place? = null
        places.forEach { place ->
            val placeLocation = Location(null)
            placeLocation.latitude = place.location.latitude
            placeLocation.longitude = place.location.longitude
            val distance = location.distanceTo(placeLocation)
            if (distance < minDistance) {
                minDistance = distance
                nearestPlace = place
            }
        }

        if (minDistance < 10) {
            return nearestPlace
        }

        return null

    }

    fun setPlaceSeen(id: String) {
        viewModelScope.launch {
            walkUseCases.setPlaceSeenUseCase(id)
        }
    }

    fun getPlace(placeId: String?): Place? {
        return places.find { it.id == placeId }
    }

    suspend fun startPersonalQuest() =
        personalQuestUseCases.startPersonalQuestUseCase(Firebase.auth.currentUser!!.uid)

}