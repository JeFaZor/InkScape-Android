package com.example.inkscape.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.LocationOn
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

data class LocationSearchData(
    val centerLatitude: Double,
    val centerLongitude: Double,
    val radiusKm: Int,
    val locationName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchFilter(
    onLocationSelected: (String, Int) -> Unit, // locationName, radiusKm
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Tel Aviv as default location
    val telAvivLocation = LatLng(32.0853, 34.7818)

    var selectedLocation by remember { mutableStateOf(telAvivLocation) }
    var radiusKm by remember { mutableStateOf(10) }
    var locationName by remember { mutableStateOf("Tel Aviv") }
    var isLoadingLocation by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(telAvivLocation, 11f)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                getCurrentLocation(context, scope) { location ->
                    selectedLocation = location
                    locationName = "Current Location"

                    scope.launch {
                        cameraPositionState.animate(
                            update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(location, 12f),
                            durationMs = 1000
                        )
                    }
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
            .height(450.dp), // Slightly bigger for radius slider
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color(0xFF9C27B0),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Search Area",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                // Current Location button
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
                    modifier = Modifier.size(40.dp)
                ) {
                    if (isLoadingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF9C27B0),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Current Location",
                            tint = Color(0xFF9C27B0),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Radius slider
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Search Radius:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFD1C4E9)
                    )
                    Text(
                        text = "$radiusKm km",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9C27B0)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = radiusKm.toFloat(),
                    onValueChange = { radiusKm = it.toInt() },
                    valueRange = 5f..50f,
                    steps = 8, // 5, 10, 15, 20, 25, 30, 35, 40, 45, 50
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF9C27B0),
                        activeTrackColor = Color(0xFF9C27B0),
                        inactiveTrackColor = Color(0xFF424242)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Radius indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "5km",
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E)
                    )
                    Text(
                        text = "25km",
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E)
                    )
                    Text(
                        text = "50km",
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }

            // Map with radius circle
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    selectedLocation = latLng
                    locationName = "${String.format("%.4f", latLng.latitude)}, ${String.format("%.4f", latLng.longitude)}"
                }
            ) {
                // Center marker
                Marker(
                    state = MarkerState(position = selectedLocation),
                    title = "Search Center"
                )

                // Radius circle
                Circle(
                    center = selectedLocation,
                    radius = radiusKm * 1000.0, // Convert km to meters
                    fillColor = Color(0x309C27B0), // Semi-transparent purple
                    strokeColor = Color(0xFF9C27B0),
                    strokeWidth = 2f
                )
            }

            // Selected location info and confirm button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A2A)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Search Center:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF9C27B0)
                            )
                            Text(
                                text = locationName,
                                fontSize = 14.sp,
                                color = Color.White,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Text(
                                text = "Within $radiusKm km radius",
                                fontSize = 12.sp,
                                color = Color(0xFFD1C4E9),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Button(
                            onClick = {
                                onLocationSelected(locationName, radiusKm)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9C27B0)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "Apply",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
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