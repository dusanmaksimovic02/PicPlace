package com.example.picplace.models.place

class MockPlaceViewModel : PlaceViewModel() {

    override fun addPlace(
        place: Place,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        onSuccess("Place added successfully!")
    }

    override suspend fun getPlaces(
        onSuccess: (List<PlaceFirebase>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        onSuccess(emptyList())
    }
}