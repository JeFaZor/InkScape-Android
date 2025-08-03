package com.example.inkscape.firebase

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*

data class ArtistProfile(
    val id: String = "",
    val profileImageUrl: String = "",
    val workImageUrls: List<String> = emptyList(),
    val styles: List<String> = emptyList(),
    val location: String = "", // "Tel Aviv, Israel"
    val placeId: String = "", // Google Place ID
    val address: String = "", // Full address
    val createdAt: Long = System.currentTimeMillis()
)

class FirebaseManager {
    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val storageRef = storage.reference
    private val artistsCollection = firestore.collection("artists")

    /**
     * Upload image to Firebase Storage
     */
    suspend fun uploadImage(imageUri: Uri, path: String): String {
        return try {
            val imageRef = storageRef.child(path)
            val uploadTask = imageRef.putFile(imageUri).await()
            val downloadUrl = imageRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            throw Exception("Failed to upload image: ${e.message}")
        }
    }

    /**
     * Create complete artist profile with images and data
     */
    suspend fun createArtistProfile(
        profileImageUri: Uri?,
        workImageUris: List<Uri?>,
        selectedStyles: List<String>,
        location: String? = null,
        placeId: String? = null,
        address: String? = null
    ): String {
        return try {
            val artistId = UUID.randomUUID().toString()

            // 1. Upload profile image
            val profileImageUrl = if (profileImageUri != null) {
                uploadImage(profileImageUri, "artists/$artistId/profile.jpg")
            } else ""

            // 2. Upload work images
            val workImageUrls = mutableListOf<String>()
            workImageUris.forEachIndexed { index, uri ->
                if (uri != null) {
                    val url = uploadImage(uri, "artists/$artistId/work_$index.jpg")
                    workImageUrls.add(url)
                }
            }

            // 3. Create artist profile object
            val artistProfile = ArtistProfile(
                id = artistId,
                profileImageUrl = profileImageUrl,
                workImageUrls = workImageUrls,
                styles = selectedStyles,
                location = location ?: "",
                placeId = placeId ?: "",
                address = address ?: ""
            )

            // 4. Save to Firestore
            artistsCollection.document(artistId).set(artistProfile).await()

            artistId
        } catch (e: Exception) {
            throw Exception("Failed to create artist profile: ${e.message}")
        }
    }

    /**
     * Get all artists from database
     */
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

    /**
     * Search artists by tattoo style
     */
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

    /**
     * Delete artist profile (for development purposes)
     */
    suspend fun deleteArtistProfile(artistId: String): Boolean {
        return try {
            // Delete document from Firestore
            artistsCollection.document(artistId).delete().await()

            // Delete images from Storage
            val artistStorageRef = storageRef.child("artists/$artistId")
            try {
                // Try to delete the folder (doesn't always work)
                artistStorageRef.delete().await()
            } catch (e: Exception) {
                // If deletion fails, it's okay - not critical
            }

            true
        } catch (e: Exception) {
            false
        }
    }
}