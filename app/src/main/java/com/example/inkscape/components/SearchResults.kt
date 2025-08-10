package com.example.inkscape.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.inkscape.firebase.ArtistProfile
import com.example.inkscape.firebase.FirebaseManager
import kotlinx.coroutines.launch
import kotlin.math.*
import androidx.compose.foundation.clickable

// Search function to handle different search criteria
private suspend fun searchArtists(
    firebaseManager: FirebaseManager,
    searchQuery: String,
    selectedStyle: String?,
    selectedLocation: String?,
    selectedRadius: Int,
    selectedLatitude: Double? = null,
    selectedLongitude: Double? = null,
    onLoading: (Boolean) -> Unit,
    onResults: (List<ArtistProfile>) -> Unit
) {
    onLoading(true)

    try {
        var results = emptyList<ArtistProfile>()

        when {
            // Search by style (priority)
            selectedStyle != null -> {
                results = firebaseManager.getArtistsByStyle(selectedStyle)

                // If location is also selected, filter by location
                if (selectedLocation != null && selectedLatitude != null && selectedLongitude != null) {
                    results = results.filter { artist ->
                        if (artist.latitude == 0.0 && artist.longitude == 0.0) {
                            false
                        } else {
                            val distance = calculateDistance(
                                selectedLatitude, selectedLongitude,
                                artist.latitude, artist.longitude
                            )
                            distance <= selectedRadius
                        }
                    }
                }
            }

            // Search by location only
            selectedLocation != null && selectedLatitude != null && selectedLongitude != null -> {
                results = firebaseManager.getArtistsByLocation(
                    centerLatitude = selectedLatitude,
                    centerLongitude = selectedLongitude,
                    radiusKm = selectedRadius.toDouble()
                )
            }

            // Search by name/query
            searchQuery.isNotEmpty() -> {
                // Get all artists and filter by studio name or other fields
                val allArtists = firebaseManager.getAllArtists()
                results = allArtists.filter { artist ->
                    artist.studioName.contains(searchQuery, ignoreCase = true) ||
                            artist.address.contains(searchQuery, ignoreCase = true) ||
                            artist.fullName.contains(searchQuery, ignoreCase = true)
                }
            }

            // Default - get all artists
            else -> {
                results = firebaseManager.getAllArtists()
            }
        }

        onResults(results)
    } catch (e: Exception) {
        onResults(emptyList())
    } finally {
        onLoading(false)
    }
}

// Helper function to calculate distance between two points
private fun calculateDistance(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val earthRadius = 6371.0 // Earth's radius in kilometers

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c
}

@Composable
fun SearchResults(
    searchQuery: String,
    selectedStyle: String?,
    selectedLocation: String?,
    selectedRadius: Int,
    selectedLatitude: Double? = null,
    selectedLongitude: Double? = null,
    modifier: Modifier = Modifier
) {
    var artists by remember { mutableStateOf<List<ArtistProfile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }

    val firebaseManager = remember { FirebaseManager() }
    val scope = rememberCoroutineScope()

    // Trigger search when parameters change
    LaunchedEffect(searchQuery, selectedStyle, selectedLocation, selectedRadius, selectedLatitude, selectedLongitude) {
        if (searchQuery.isNotEmpty() || selectedStyle != null || selectedLocation != null) {
            scope.launch {
                searchArtists(
                    firebaseManager = firebaseManager,
                    searchQuery = searchQuery,
                    selectedStyle = selectedStyle,
                    selectedLocation = selectedLocation,
                    selectedRadius = selectedRadius,
                    selectedLatitude = selectedLatitude,
                    selectedLongitude = selectedLongitude,
                    onLoading = { isLoading = it },
                    onResults = { results ->
                        artists = results
                        hasSearched = true
                    }
                )
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Search header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {


                // Show search criteria
                val searchCriteria = buildList {
                    if (searchQuery.isNotEmpty()) add("\"$searchQuery\"")
                    if (selectedStyle != null) add("Style: $selectedStyle")
                    if (selectedLocation != null) add("Near $selectedLocation")
                }

                if (searchCriteria.isNotEmpty()) {
                    Text(
                        text = "Found ${artists.size} artists for: ${searchCriteria.joinToString(" • ")}",
                        fontSize = 12.sp,
                        color = Color(0xFFD1C4E9),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Results content
        when {
            isLoading -> {
                // Loading state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF9C27B0),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Searching artists...",
                            fontSize = 16.sp,
                            color = Color(0xFFD1C4E9),
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }

            hasSearched && artists.isEmpty() -> {
                // No results state
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1A1A)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No artists found",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "Try adjusting your search criteria",
                                fontSize = 14.sp,
                                color = Color(0xFFD1C4E9),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            artists.isNotEmpty() -> {
                // Results grid
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(artists) { artist ->
                        ArtistResultCard(artist = artist)
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistResultCard(
    artist: ArtistProfile,
    modifier: Modifier = Modifier
) {
    var showImageDialog by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf("") }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Profile image and name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile image
                AsyncImage(
                    model = artist.profileImageUrl.takeIf { it.isNotEmpty() },
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    fallback = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_camera)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Artist name
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    val displayName = if (artist.studioName.isNotEmpty()) {
                        artist.studioName
                    } else {
                        "Artist"
                    }

                    Text(
                        text = artist.fullName ?: displayName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Work images - 3 in a row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(3) { index ->
                    val imageUrl = if (index < artist.workImageUrls.size) {
                        artist.workImageUrls[index]
                    } else {
                        null
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.8f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2A2A2A))
                            .clickable {
                                if (imageUrl != null && imageUrl.isNotEmpty()) {
                                    selectedImageUrl = imageUrl
                                    showImageDialog = true
                                }
                            }
                    ) {
                        if (imageUrl != null && imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Work Image ${index + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Styles section
            if (artist.styles.isNotEmpty()) {
                Text(
                    text = artist.styles.take(3).joinToString(" • "),
                    fontSize = 14.sp,
                    color = Color(0xFF9C27B0),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Location section
            if (artist.address.isNotEmpty()) {
                Text(
                    text = artist.address,
                    fontSize = 13.sp,
                    color = Color(0xFFD1C4E9),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }
        }
    }

    // Image dialog
    if (showImageDialog && selectedImageUrl.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showImageDialog = false },
            title = { Text("Work Image", color = Color.White) },
            text = {
                AsyncImage(
                    model = selectedImageUrl,
                    contentDescription = "Work Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showImageDialog = false }
                ) {
                    Text("Close", color = Color(0xFF9C27B0))
                }
            },
            containerColor = Color(0xFF1A1A1A)
        )
    }
}
