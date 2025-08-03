package com.example.inkscape.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import com.example.inkscape.R
import com.example.inkscape.components.StyleGrid

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    onSignInClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") }
    var showStylePicker by remember { mutableStateOf(false) }
    var selectedStyle by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Sign In/Up buttons (top right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.End
        ) {
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp)
                .animateContentSize(
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = FastOutSlowInEasing
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (showStylePicker) Arrangement.Top else Arrangement.Center
        ) {

            if (!showStylePicker) {
                Spacer(modifier = Modifier.weight(1f))
            } else {
                Spacer(modifier = Modifier.height(80.dp))
            }

            // Logo (always visible)
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "InkScape Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 32.dp)
            )

            // Title and subtitle with animation
            AnimatedVisibility(
                visible = !showStylePicker,
                enter = fadeIn(
                    animationSpec = tween(300, delayMillis = 100)
                ) + expandVertically(
                    animationSpec = tween(400)
                ),
                exit = fadeOut(
                    animationSpec = tween(200)
                ) + shrinkVertically(
                    animationSpec = tween(300)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Main title
                    Text(
                        text = "Find Your Perfect",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 42.sp
                    )

                    Text(
                        text = "Tattoo Artist",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9C27B0),
                        textAlign = TextAlign.Center,
                        lineHeight = 42.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Subtitle
                    Text(
                        text = "Discover talented tattoo artists based on their unique styles and specialties. Search by location or explore to find the perfect match.",
                        fontSize = 16.sp,
                        color = Color(0xFFD1C4E9),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(bottom = 48.dp)
                    )
                }
            }

            // Search field
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

            // Search button
            Button(
                onClick = { /* TODO: Search action */ },
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

            Spacer(modifier = Modifier.height(40.dp))

            // Filter buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Style button
                OutlinedButton(
                    onClick = {
                        showStylePicker = !showStylePicker
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

                // Location button
                OutlinedButton(
                    onClick = { /* TODO: Location picker */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color(0xFF424242)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Location",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Style picker (when expanded)
            AnimatedVisibility(
                visible = showStylePicker,
                enter = fadeIn(
                    animationSpec = tween(300, delayMillis = 200)
                ) + expandVertically(
                    animationSpec = tween(400)
                ),
                exit = fadeOut(
                    animationSpec = tween(200)
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

            // Dynamic spacer for bottom
            if (!showStylePicker) {
                Spacer(modifier = Modifier.weight(1f))

                // Bottom text
                Text(
                    text = "Join thousands of tattoo enthusiasts finding their perfect artist",
                    fontSize = 14.sp,
                    color = Color(0xFF9E9E9E),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}