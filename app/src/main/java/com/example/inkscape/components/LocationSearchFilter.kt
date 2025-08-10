package com.example.inkscape.components

import android.Manifest
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import java.util.Locale

data class LocationSearchData(
    val centerLatitude: Double,
    val centerLongitude: Double,
    val radiusKm: Int,
    val locationName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchFilter(
    onLocationSelected: (String, Int, Double, Double) -> Unit, // locationName, radiusKm, latitude, longitude
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Default: Tel Aviv
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
            val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            if (fine || coarse) {
                getCurrentLocation(context, scope) { location ->
                    selectedLocation = location
                    getAddressFromLocation(context, scope, location) { address ->
                        locationName = address
                    }
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
        modifier = modifier.fillMaxWidth(), // height controlled by parent screen
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header — compact
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Color(0xFF9C27B0),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Search Area",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            // Radius slider — above the map, slim
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .padding(bottom = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Search Radius",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFD1C4E9)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // – 2 km
                        CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                            TextButton(
                                onClick = { radiusKm = (radiusKm - 2).coerceIn(2, 40) },
                                modifier = Modifier.height(28.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) { Text("–", fontSize = 14.sp, color = Color.White) }
                        }
                        Text(
                            text = "$radiusKm km",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9C27B0),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        // + 2 km
                        CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                            TextButton(
                                onClick = { radiusKm = (radiusKm + 2).coerceIn(2, 40) },
                                modifier = Modifier.height(28.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) { Text("+", fontSize = 14.sp, color = Color.White) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                    Slider(
                        value = radiusKm.toFloat(),
                        onValueChange = { radiusKm = it.toInt().coerceIn(2, 40) },
                        valueRange = 2f..40f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF9C27B0),
                            activeTrackColor = Color(0xFF9C27B0),
                            inactiveTrackColor = Color(0xFF424242)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(22.dp) // very slim
                    )
                }
            }

            // Map + floating controls (don't consume Column height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)               // map takes all flexible height
                    .heightIn(min = 260.dp)
            ) {
                GoogleMap(
                    modifier = Modifier.matchParentSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng ->
                        selectedLocation = latLng
                        getAddressFromLocation(context, scope, latLng) { address ->
                            locationName = address
                        }
                    }
                ) {
                    Marker(
                        state = MarkerState(position = selectedLocation),
                        title = "Search Center",
                        snippet = locationName
                    )
                    Circle(
                        center = selectedLocation,
                        radius = radiusKm * 1000.0,     // km -> meters
                        fillColor = Color(0x249C27B0), // translucent fill
                        strokeColor = Color(0xFF9C27B0),
                        strokeWidth = 1.5f
                    )
                }

                // Current Location — floating top-right
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    color = Color(0xCC2A2A2A),
                    shape = CircleShape,
                    tonalElevation = 0.dp,
                    shadowElevation = 2.dp
                ) {
                    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
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
                            modifier = Modifier.size(36.dp)
                        ) {
                            if (isLoadingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color(0xFF9C27B0),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "Current Location",
                                    tint = Color(0xFF9C27B0)
                                )
                            }
                        }
                    }
                }

                // Apply — floating bottom-center
                ElevatedButton(
                    onClick = { onLocationSelected(locationName, radiusKm, selectedLocation.latitude, selectedLocation.longitude) },
                    colors = ButtonDefaults.elevatedButtonColors(containerColor = Color(0xFF9C27B0)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                        .height(36.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text("Apply", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
                onLocationReceived(LatLng(location.latitude, location.longitude))
            }
        }
    } catch (_: SecurityException) {
        // permissions not granted
    }
}

private fun getAddressFromLocation(
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    latLng: LatLng,
    onAddressReceived: (String) -> Unit
) {
    scope.launch {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            val address = if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                buildString {
                    // City / locality
                    addr.locality?.let {
                        if (it.isNotBlank()) append(it)
                    } ?: addr.subAdminArea?.let {
                        if (it.isNotBlank()) append(it)
                    }
                    // District if different
                    addr.subAdminArea?.let { district ->
                        if (district.isNotBlank() && district != (addr.locality ?: "")) {
                            if (isNotEmpty()) append(", ")
                            append(district)
                        }
                    }
                    // Fallback to coords
                    if (isEmpty()) {
                        append("${String.format("%.4f", latLng.latitude)}, ${String.format("%.4f", latLng.longitude)}")
                    }
                }
            } else {
                "${String.format("%.4f", latLng.latitude)}, ${String.format("%.4f", latLng.longitude)}"
            }

            onAddressReceived(address)
        } catch (_: Exception) {
            onAddressReceived("${String.format("%.4f", latLng.latitude)}, ${String.format("%.4f", latLng.longitude)}")
        }
    }
}