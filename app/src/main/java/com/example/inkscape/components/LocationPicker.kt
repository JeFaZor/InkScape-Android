package com.example.inkscape.components

import android.Manifest
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.util.*

data class SelectedLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPicker(
    onLocationSelected: (SelectedLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val defaultLocation = LatLng(32.0853, 34.7818)
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var showLocationName by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }
    var isLoadingLocation by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 10f)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                getCurrentLocation(context, scope) { location ->
                    selectedLocation = location
                    showLocationName = "Current Location: ${String.format("%.4f", location.latitude)}, ${String.format("%.4f", location.longitude)}"

                    scope.launch {
                        cameraPositionState.animate(
                            update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(location, 15f),
                            durationMs = 1000
                        )
                    }

                    onLocationSelected(
                        SelectedLocation(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            address = showLocationName
                        )
                    )
                    isLoadingLocation = false
                }
            } else {
                isLoadingLocation = false
            }
        }
    )

    fun searchAddress(address: String) {
        if (address.isBlank()) return

        scope.launch {
            isLoadingLocation = true
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocationName("$address, Israel", 1)

                if (!addresses.isNullOrEmpty()) {
                    val foundLocation = addresses[0]
                    val latLng = LatLng(foundLocation.latitude, foundLocation.longitude)

                    selectedLocation = latLng
                    showLocationName = foundLocation.getAddressLine(0) ?: address

                    cameraPositionState.animate(
                        update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                        durationMs = 1000
                    )

                    onLocationSelected(
                        SelectedLocation(
                            latitude = latLng.latitude,
                            longitude = latLng.longitude,
                            address = showLocationName
                        )
                    )
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoadingLocation = false
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Color(0xFF9C27B0),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Choose Your Studio Location",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Search location...", color = Color.Gray) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF9C27B0)
                        )
                    },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(
                                onClick = { searchAddress(searchText) }
                            ) {
                                if (isLoadingLocation) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color(0xFF9C27B0)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = Color(0xFF9C27B0)
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF9C27B0),
                        unfocusedBorderColor = Color(0xFF424242),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF9C27B0),
                        focusedContainerColor = Color(0x20FFFFFF),
                        unfocusedContainerColor = Color(0x20FFFFFF)
                    ),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        isLoadingLocation = true
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    modifier = Modifier
                        .size(56.dp)
                ) {
                    if (isLoadingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF9C27B0)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Current Location",
                            tint = Color(0xFF9C27B0),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    selectedLocation = latLng
                    showLocationName = "Lat: ${String.format("%.4f", latLng.latitude)}, " +
                            "Lng: ${String.format("%.4f", latLng.longitude)}"

                    onLocationSelected(
                        SelectedLocation(
                            latitude = latLng.latitude,
                            longitude = latLng.longitude,
                            address = showLocationName
                        )
                    )
                }
            ) {
                selectedLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "Your Studio Location"
                    )
                }
            }

            if (selectedLocation != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2A2A)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Selected Location:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF9C27B0)
                        )
                        Text(
                            text = showLocationName,
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2A2A)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Tap on the map to select your studio location",
                        fontSize = 14.sp,
                        color = Color(0xFFBDBDBD),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

private fun getCurrentLocation(
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    onLocationReceived: (LatLng) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                onLocationReceived(latLng)
            }
        }
    } catch (e: SecurityException) {
        // Handle permission error
    }
}