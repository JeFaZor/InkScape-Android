package com.example.inkscape

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.inkscape.screens.AuthScreen
import com.example.inkscape.screens.HomeScreen
import com.example.inkscape.screens.ArtistSignUpScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()

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
                }
            )
        }

        composable("auth") {
            AuthScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onAuthSuccess = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
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
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}