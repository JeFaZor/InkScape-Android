package com.example.inkscape.components

import androidx.compose.foundation.Image
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

@Composable
fun SearchResults(
    searchQuery: String,
    selectedStyle: String?,
    selectedLocation: String?,
    selectedRadius: Int,
    modifier: Modifier = Modifier
) {
    var artists by remember { mutableStateOf<List<ArtistProfile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }

    val firebaseManager = remember { FirebaseManager() }
    val scope = rememberCoroutineScope()

    // Trigger search when parameters change
    LaunchedEffect(searchQuery, selectedStyle, selectedLocation, selectedRadius) {
        if (searchQuery.isNotEmpty() || selectedStyle != null || selectedLocation != null) {
            scope.launch {
                searchArtists(
                    firebaseManager = firebaseManager,
                    searchQuery = searchQuery,
                    selectedStyle = selectedStyle,
                    selectedLocation = selectedLocation,
                    selectedRadius = selectedRadius,
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
                Text(
                    text = "Search Results",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Show search criteria
                val searchCriteria = buildList {
                    if (searchQuery.isNotEmpty()) add("\"$searchQuery\"")
                    if (selectedStyle != null) add("Style: $selectedStyle")
                    if (selectedLocation != null) add("Near $selectedLocation")
                }

                if (searchCriteria.isNotEmpty()) {
                    Text(
                        text = "Found ${artists.size} artists for: ${searchCriteria.joinToString(" • ")}",
                        fontSize = 14.sp,
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
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "לא נמצאו תוצאות",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Try adjusting your search criteria",
                                fontSize = 14.sp,
                                color = Color(0xFF9E9E9E),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            artists.isNotEmpty() -> {
                // Results grid - 2 columns
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp), // Fixed height for consistent grid
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Profile image and name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile image (60dp = medium size)
                AsyncImage(
                    model = artist.profileImageUrl.takeIf { it.isNotEmpty() },
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    fallback = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_camera)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Artist name
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Extract name from studio name or use a default format
                    val displayName = if (artist.studioName.isNotEmpty()) {
                        artist.studioName
                    } else {
                        "Artist" // You might want to add actual name fields to ArtistProfile
                    }

                    Text(
                        text = displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Work images - 3 in a row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(3) { index ->
                    val imageUrl = artist.workImageUrls.getOrNull(index)

                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Work ${index + 1}",
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        fallback = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Styles (bottom section)
            if (artist.styles.isNotEmpty()) {
                Text(
                    text = "Specializes in:",
                    fontSize = 12.sp,
                    color = Color(0xFF9C27B0),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Show first 2-3 styles with overflow handling
                val displayStyles = artist.styles.take(3)
                val hasMoreStyles = artist.styles.size > 3

                Text(
                    text = if (hasMoreStyles) {
                        "${displayStyles.joinToString(", ")} +${artist.styles.size - 3} more"
                    } else {
                        displayStyles.joinToString(", ")
                    },
                    fontSize = 11.sp,
                    color = Color(0xFFD1C4E9),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f)) // Push content up
        }
    }
}

private suspend fun searchArtists(
    firebaseManager: FirebaseManager,
    searchQuery: String,
    selectedStyle: String?,
    selectedLocation: String?,
    selectedRadius: Int,
    onLoading: (Boolean) -> Unit,
    onResults: (List<ArtistProfile>) -> Unit
) {
    onLoading(true)

    try {
        var results = emptyList<ArtistProfile>()

        when {
            // Search by style
            selectedStyle != null -> {
                results = firebaseManager.getArtistsByStyle(selectedStyle)
            }

            // Search by location (you'll need to implement location-based search)
            selectedLocation != null && selectedLocation != "Current Location" -> {
                // For now, get all artists and filter by distance
                // You might want to implement a more efficient location-based query
                results = firebaseManager.getAllArtists()
                // TODO: Filter by location and radius
            }

            // Search by name/query
            searchQuery.isNotEmpty() -> {
                // Get all artists and filter by studio name or other fields
                val allArtists = firebaseManager.getAllArtists()
                results = allArtists.filter { artist ->
                    artist.studioName.contains(searchQuery, ignoreCase = true) ||
                            artist.address.contains(searchQuery, ignoreCase = true)
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