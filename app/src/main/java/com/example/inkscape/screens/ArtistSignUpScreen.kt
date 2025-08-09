package com.example.inkscape.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.inkscape.firebase.FirebaseManager
import com.example.inkscape.components.LocationPicker
import com.example.inkscape.components.SelectedLocation
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistSignUpScreen(
    onBackClick: () -> Unit = {},
    onSignUpComplete: () -> Unit = {}
) {
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var workImages by remember { mutableStateOf(listOf<Uri?>(null, null, null)) }
    var selectedStyles by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedLocation by remember { mutableStateOf<SelectedLocation?>(null) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var uploadStatus by remember { mutableStateOf("") }

    val firebaseManager = remember { FirebaseManager() }
    val scope = rememberCoroutineScope()

    val availableStyles = listOf(
        "Traditional", "New School", "Japanese", "Fineline",
        "Geometric", "Micro Realism", "Realism", "Dot Work",
        "Dark Art", "Flowers", "Surrealism", "Trash Polka"
    )

    val profileImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
    }

    val workImagePickers = (0..2).map { index ->
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            workImages = workImages.toMutableList().apply {
                this[index] = uri
            }
        }
    }

    fun uploadToFirebase() {
        scope.launch {
            try {
                isUploading = true
                uploadStatus = "Uploading profile..."

                val artistId = firebaseManager.createArtistProfile(
                    email = email,
                    password = password,
                    profileImageUri = profileImageUri,
                    workImageUris = workImages,
                    selectedStyles = selectedStyles,
                    location = selectedLocation?.address,
                    placeId = null,
                    address = selectedLocation?.address,
                    latitude = selectedLocation?.latitude ?: 0.0,
                    longitude = selectedLocation?.longitude ?: 0.0,
                    studioName = null
                )

                uploadStatus = "Success! Profile created with ID: $artistId"
                kotlinx.coroutines.delay(2000)
                onSignUpComplete()
            } catch (e: Exception) {
                uploadStatus = "Error: ${e.message}"
                isUploading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(16.dp)
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Spacer(modifier = Modifier.height(60.dp)) }

            item {
                Text(
                    text = "Set Up Your Artist Profile",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                Text(
                    text = "Add your profile info and showcase your work",
                    fontSize = 16.sp,
                    color = Color(0xFFD1C4E9),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 40.dp)
                )
            }

            if (uploadStatus.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (uploadStatus.startsWith("Error"))
                                Color(0xFFFF5252) else Color(0xFF4CAF50)
                        )
                    ) {
                        Text(
                            text = uploadStatus,
                            modifier = Modifier.padding(16.dp),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Profile Picture",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF424242))
                            .clickable {
                                if (!isUploading) profileImagePicker.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileImageUri != null) {
                            AsyncImage(
                                model = profileImageUri,
                                contentDescription = "Profile Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Add Profile Picture",
                                    tint = Color(0xFF9C27B0),
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = "Add Photo",
                                    color = Color(0xFF9C27B0),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Your Work (Up to 3 images)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        workImages.forEachIndexed { index, uri ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF424242))
                                    .clickable {
                                        if (!isUploading) workImagePickers[index].launch("image/*")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (uri != null) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Work Image ${index + 1}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add Work Image",
                                            tint = Color(0xFF9C27B0),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "Add",
                                            color = Color(0xFF9C27B0),
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Studio Location",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LocationPicker(
                        onLocationSelected = { location ->
                            selectedLocation = location
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Your Specialties (Choose up to 3)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Selected: ${selectedStyles.size}/3",
                        fontSize = 14.sp,
                        color = Color(0xFFD1C4E9),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }

            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    items(availableStyles) { style ->
                        val isSelected = selectedStyles.contains(style)
                        val canSelect = selectedStyles.size < 3 || isSelected

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = canSelect && !isUploading) {
                                    selectedStyles = if (isSelected) {
                                        selectedStyles - style
                                    } else if (selectedStyles.size < 3) {
                                        selectedStyles + style
                                    } else {
                                        selectedStyles
                                    }
                                }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF9C27B0) else Color(0xFF424242),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0x30FFFFFF) else Color(0x10FFFFFF)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = style,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (canSelect) Color.White else Color(0xFF666666),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }

            item {
                Button(
                    onClick = { uploadToFirebase() },
                    enabled = canComplete(profileImageUri, workImages, selectedStyles, selectedLocation) && !isUploading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9C27B0),
                        disabledContainerColor = Color(0xFF666666)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Complete Profile",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

private fun canComplete(
    profileImage: Uri?,
    workImages: List<Uri?>,
    selectedStyles: List<String>,
    selectedLocation: SelectedLocation?
): Boolean {
    return profileImage != null &&
            workImages.any { it != null } &&
            selectedStyles.isNotEmpty() &&
            selectedLocation != null
}