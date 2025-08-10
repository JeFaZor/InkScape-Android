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

    val defaultLocation = LatLng(32.0853, 34.7818) // Tel Aviv center
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var displayLocationName by remember { mutableStateOf("") }
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
                    handleLocationSelection(location, context, scope,
                        onLocationUpdate = { lat, lng, address ->
                            selectedLocation = LatLng(lat, lng)
                            displayLocationName = address
                            onLocationSelected(SelectedLocation(lat, lng, address))
                        },
                        onCameraUpdate = { latLng ->
                            scope.launch {
                                cameraPositionState.animate(
                                    update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                                    durationMs = 1000
                                )
                            }
                        }
                    )
                    isLoadingLocation = false
                }
            } else {
                isLoadingLocation = false
            }
        }
    )

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
            // Header
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
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        isLoadingLocation = true
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9C27B0)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoadingLocation
                ) {
                    if (isLoadingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Getting location...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Current Location",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Use Current Location")
                    }
                }
            }

            // Map
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    handleLocationSelection(latLng, context, scope,
                        onLocationUpdate = { lat, lng, address ->
                            selectedLocation = LatLng(lat, lng)
                            displayLocationName = address
                            onLocationSelected(SelectedLocation(lat, lng, address))
                        },
                        onCameraUpdate = { }
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

            // Selected Location Display
            if (selectedLocation != null && displayLocationName.isNotEmpty()) {
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
                            text = displayLocationName,
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
                        text = "Use current location or tap on the map to select your studio location",
                        fontSize = 14.sp,
                        color = Color(0xFFBDBDBD),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

private fun handleLocationSelection(
    latLng: LatLng,
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    onLocationUpdate: (Double, Double, String) -> Unit,
    onCameraUpdate: (LatLng) -> Unit
) {
    scope.launch {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            val formattedAddress = if (!addresses.isNullOrEmpty()) {
                formatAddressForDisplay(addresses[0])
            } else {
                "Selected location"
            }

            onLocationUpdate(latLng.latitude, latLng.longitude, formattedAddress)
            onCameraUpdate(latLng)
        } catch (e: Exception) {
            onLocationUpdate(latLng.latitude, latLng.longitude, "Selected location")
        }
    }
}

private fun formatAddressForDisplay(address: android.location.Address): String {
    val components = mutableListOf<String>()

    // Add street address if available
    address.getAddressLine(0)?.let { addressLine ->
        val parts = addressLine.split(",")
        if (parts.isNotEmpty()) {
            val streetPart = parts[0].trim()
            if (streetPart.isNotBlank()) {
                components.add(streetPart)
            }
        }
    }

    address.locality?.let { if (it.isNotBlank()) components.add(it) }
        ?: address.subAdminArea?.let { if (it.isNotBlank()) components.add(it) }

    return if (components.isNotEmpty()) {
        components.joinToString(", ")
    } else {
        "Selected location"
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