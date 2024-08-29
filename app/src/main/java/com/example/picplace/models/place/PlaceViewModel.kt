package com.example.picplace.models.place

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.picplace.models.auth.AuthViewModel.Companion.isPreviewMode
import com.example.picplace.ui.screens.map.addplace.PollObjects
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

open class PlaceViewModel : ViewModel() {
    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    init {
        if (!isPreviewMode) {
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
        }
    }

    open fun addPlace(place: Place, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {

    }

    fun getPlaces() {

    }

    fun getPlacesForSpecificUser() {

    }

    fun updatePlace() {

    }

    fun deletePlace() {

    }

    fun likePlace() {

    }

    fun commentPlace() {

    }
}

data class PlaceFirebase(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrls: List<String> = emptyList(),
    val likes: Int = 0,
    val likedBy: List<String> = emptyList(), // List of user IDs who liked the place
    val comments: List<Comment> = emptyList(),
    val poll: List<PollObjects> = emptyList(),
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Place(
    val name: String = "",
    val description: String = "",
    val imageUris: List<Uri> = emptyList(),
    val poll: List<PollObjects> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class Comment(
    val userId: String = "",
    val userName: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)