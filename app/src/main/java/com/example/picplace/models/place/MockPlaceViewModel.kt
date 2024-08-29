package com.example.picplace.models.place

class MockPlaceViewModel : PlaceViewModel() {

    override fun addPlace(
        place: Place,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        onSuccess("Place added successfully!")
    }
}