package com.example.inkscape.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import coil.compose.AsyncImage
import com.example.inkscape.R
import com.example.inkscape.components.StyleGrid
import com.example.inkscape.components.LocationSearchFilter
import com.example.inkscape.components.SearchResults

// Data class for user state
data class UserState(
    val userId: String,
    val fullName: String,
    val profileImageUrl: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    onSignInClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    currentUser: UserState? = null
) {
    var searchText by remember { mutableStateOf("") }
    var showStylePicker by remember { mutableStateOf(false) }
    var selectedStyle by remember { mutableStateOf<String?>(null) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<String?>(null) }
    var showSearchResults by remember { mutableStateOf(false) }
    var selectedLatitude by remember { mutableStateOf<Double?>(null) }
    var selectedLongitude by remember { mutableStateOf<Double?>(null) }
    var selectedRadius by remember { mutableStateOf(10) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top bar with logo and auth buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo on the left
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "InkScape Logo",
                modifier = Modifier.size(120.dp)
            )

            // Auth buttons on the right
            if (currentUser != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF424242)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentUser.profileImageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = currentUser.profileImageUrl,
                                contentDescription = "Profile Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = currentUser.fullName,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    OutlinedButton(
                        onClick = onLogoutClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = Color(0xFF9C27B0)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Logout",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                Row {
                    OutlinedButton(
                        onClick = onSignInClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = Color(0xFF9C27B0)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Sign In",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onSignUpClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9C27B0)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Sign Up",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 140.dp)
                .animateContentSize(
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = FastOutSlowInEasing
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!showStylePicker && !showLocationPicker && !showSearchResults) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Find Your Perfect",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Tattoo Artist",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9C27B0),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Discover talented artists, explore styles, and book your next masterpiece",
                        fontSize = 16.sp,
                        color = Color(0xFFD1C4E9),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(bottom = 48.dp)
                    )
                }
            }

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = {
                    Text(
                        "Search artist by name or style...",
                        color = Color(0xFFBDBDBD)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF9C27B0)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
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

            Button(
                onClick = {
                    if (searchText.isNotEmpty() || selectedStyle != null || selectedLocation != null) {
                        showSearchResults = true
                        showStylePicker = false
                        showLocationPicker = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9C27B0)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Search Artists",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        showStylePicker = !showStylePicker
                        showLocationPicker = false
                        showSearchResults = false
                        if (!showStylePicker) {
                            selectedStyle = null
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedStyle != null || showStylePicker)
                            Color(0x30FFFFFF) else Color.Transparent,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (selectedStyle != null || showStylePicker)
                            Color(0xFF9C27B0) else Color(0xFF424242)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = selectedStyle ?: "Style",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedButton(
                    onClick = {
                        showLocationPicker = !showLocationPicker
                        showStylePicker = false
                        showSearchResults = false
                        if (!showLocationPicker) {
                            selectedLocation = null
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedLocation != null || showLocationPicker)
                            Color(0x30FFFFFF) else Color.Transparent,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (selectedLocation != null || showLocationPicker)
                            Color(0xFF9C27B0) else Color(0xFF424242)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = selectedLocation ?: "Location",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            AnimatedVisibility(
                visible = showStylePicker,
                enter = expandVertically(
                    animationSpec = tween(300)
                ) + fadeIn(
                    animationSpec = tween(300)
                ),
                exit = fadeOut(
                    animationSpec = tween(300)
                ) + shrinkVertically(
                    animationSpec = tween(300)
                )
            ) {
                Column {
                    Spacer(modifier = Modifier.height(24.dp))
                    StyleGrid(
                        onStyleSelected = { style ->
                            selectedStyle = style
                            showStylePicker = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            AnimatedVisibility(
                visible = showLocationPicker,
                enter = expandVertically(
                    animationSpec = tween(300)
                ) + fadeIn(
                    animationSpec = tween(300)
                ),
                exit = fadeOut(
                    animationSpec = tween(300)
                ) + shrinkVertically(
                    animationSpec = tween(300)
                )
            ) {
                Column {
                    Spacer(modifier = Modifier.height(24.dp))
                    LocationSearchFilter(
                        onLocationSelected = { locationName, radiusKm, latitude, longitude ->
                            selectedLocation = locationName
                            selectedRadius = radiusKm
                            selectedLatitude = latitude
                            selectedLongitude = longitude
                            showLocationPicker = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = showSearchResults,
                enter = expandVertically(
                    animationSpec = tween(300)
                ) + fadeIn(
                    animationSpec = tween(300)
                ),
                exit = fadeOut(
                    animationSpec = tween(300)
                ) + shrinkVertically(
                    animationSpec = tween(300)
                )
            ) {
                Column {
                    Spacer(modifier = Modifier.height(24.dp))

                    SearchResults(
                        searchQuery = searchText,
                        selectedStyle = selectedStyle,
                        selectedLocation = selectedLocation,
                        selectedRadius = selectedRadius,
                        selectedLatitude = selectedLatitude,
                        selectedLongitude = selectedLongitude,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }


        }
    }
}