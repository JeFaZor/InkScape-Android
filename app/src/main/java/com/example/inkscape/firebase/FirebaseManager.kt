package com.example.inkscape.firebase

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import java.util.*

data class ArtistProfile(
    val id: String = "",
    val profileImageUrl: String = "",
    val workImageUrls: List<String> = emptyList(),
    val styles: List<String> = emptyList(),
    val location: String = "",
    val placeId: String = "",
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

class FirebaseManager {
    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val storageRef = storage.reference
    private val artistsCollection = firestore.collection("artists")

    private suspend fun uploadImage(imageUri: Uri, path: String): String {
        return try {
            val imageRef = storageRef.child(path)
            val uploadTask = imageRef.putFile(imageUri).await()
            val downloadUrl = imageRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            throw Exception("Failed to upload image: ${e.message}")
        }
    }

    suspend fun createArtistProfile(
        email: String,
        password: String,
        profileImageUri: Uri?,
        workImageUris: List<Uri?>,
        selectedStyles: List<String>,
        location: String? = null
    ): String {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("Failed to create user")

            val artistId = user.uid

            val profileImageUrl = if (profileImageUri != null) {
                uploadImage(profileImageUri, "artists/$artistId/profile.jpg")
            } else ""

            val workImageUrls = mutableListOf<String>()
            workImageUris.forEachIndexed { index, uri ->
                if (uri != null) {
                    val url = uploadImage(uri, "artists/$artistId/work_$index.jpg")
                    workImageUrls.add(url)
                }
            }

            val artistProfile = ArtistProfile(
                id = artistId,
                profileImageUrl = profileImageUrl,
                workImageUrls = workImageUrls,
                styles = selectedStyles,
                location = location ?: ""
            )

            artistsCollection.document(artistId).set(artistProfile).await()
            artistId
        } catch (e: Exception) {
            throw Exception("Failed to create artist profile: ${e.message}")
        }
    }

    suspend fun signIn(email: String, password: String): String {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            authResult.user?.uid ?: throw Exception("Login failed")
        } catch (e: Exception) {
            throw Exception("Login failed: ${e.message}")
        }
    }

    suspend fun getAllArtists(): List<ArtistProfile> {
        return try {
            val snapshot = artistsCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(ArtistProfile::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getArtistsByStyle(style: String): List<ArtistProfile> {
        return try {
            val snapshot = artistsCollection
                .whereArrayContains("styles", style)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(ArtistProfile::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}