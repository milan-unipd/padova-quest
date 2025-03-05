package it.unipd.milan.padovaquest.feature_walking.domain.use_case

data class WalkUseCases(
    val getPlacesWithinBoundsUseCase: GetPlacesWithinBoundsUseCase,
    val setPlaceSeenUseCase: SetPlaceSeenUseCase,
    val getPlaceDescriptionUseCase: GetPlaceDescriptionUseCase
)
