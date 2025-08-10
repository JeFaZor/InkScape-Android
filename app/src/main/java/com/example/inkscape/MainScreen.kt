package com.example.inkscape

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.inkscape.screens.AuthScreen
import com.example.inkscape.screens.HomeScreen
import com.example.inkscape.screens.ArtistSignUpScreen
import com.example.inkscape.screens.UserState

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    // State to track logged in user
    var currentUser by remember { mutableStateOf<UserState?>(null) }

    // Helper function to format name with proper capitalization
    fun formatName(fullName: String): String {
        return fullName.split(" ")
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase() else char.toString()
                }
            }
    }

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onSignInClick = {
                    navController.navigate("auth")
                },
                onSignUpClick = {
                    navController.navigate("artist_signup")
                },
                onLogoutClick = {
                    // Clear user state and stay on home
                    currentUser = null
                },
                currentUser = currentUser
            )
        }

        composable("auth") {
            AuthScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onAuthSuccess = { userId, fullName, profileImageUrl ->
                    if (userId.isEmpty()) {
                        // Sign up was clicked - go to artist signup
                        navController.navigate("artist_signup")
                    } else {
                        // Successful login - update user state and go to home
                        currentUser = UserState(
                            userId = userId,
                            fullName = formatName(fullName),
                            profileImageUrl = profileImageUrl
                        )
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("artist_signup") {
            ArtistSignUpScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSignUpComplete = {
                    // After successful signup, go back to home
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}