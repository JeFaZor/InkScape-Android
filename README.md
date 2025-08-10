# INKSCAPE-ANDROID

Android application for finding and connecting with tattoo artists - part of the Inkscape platform.

## Overview

INKSCAPE-ANDROID is a modern Android application built with Kotlin and Jetpack Compose, enabling users to search for tattoo artists in their area based on styles, location, and ratings.

## Tech Stack

- **Kotlin** - Primary development language
- **Jetpack Compose** - Modern UI toolkit
- **Navigation Compose** - Screen navigation
- **Firebase** - Authentication, database, and file storage
- **Google Maps** - Location services and mapping
- **Material Design 3** - UI design system

## Project Structure

### Main Screens
- **HomeScreen** - Main search and filtering interface
- **AuthScreen** - Sign in and authentication
- **ArtistSignUpScreen** - Artist registration flow

### Key Components
- **LocationPicker** - Interactive map for location selection
- **LocationSearchFilter** - Location-based search with radius
- **StyleGrid** - Tattoo style selection interface
- **SearchResults** - Artist search results display

### Firebase Integration
- **FirebaseManager** - Handles artist profile creation and data management
- **Authentication** - User sign-in/sign-up functionality
- **Storage** - Image upload for profiles and portfolios
- **Firestore** - Real-time database for artist data

## Features

### User Features
- Search artists by tattoo style and location
- Interactive map-based location selection
- Radius-based geographic filtering
- Artist profile viewing with portfolios
- User authentication and profiles

### Artist Features
- Profile creation with portfolio upload
- Style specialization selection
- Location and service area setup
- Instagram integration capability

## Database Structure

The app integrates with a Supabase backend containing:
- **users** - Basic user information
- **artist_profiles** - Extended artist data
- **styles** - Tattoo style catalog
- **reviews** - User ratings and feedback
- **search_history** - User search tracking

## Dependencies

### Core Android
- androidx.core:core-ktx
- androidx.lifecycle:lifecycle-runtime-ktx
- androidx.activity:activity-compose

### Compose UI
- androidx.compose.bom
- androidx.compose.ui
- androidx.compose.material3
- androidx.compose.material:material-icons-extended

### Navigation & Architecture
- androidx.navigation:navigation-compose
- androidx.lifecycle:lifecycle-viewmodel-compose

### Firebase Services
- firebase-bom:34.0.0
- firebase-analytics
- firebase-storage
- firebase-firestore
- firebase-auth

### Maps & Location
- com.google.maps.android:maps-compose:4.3.3
- com.google.android.gms:play-services-maps:18.2.0
- com.google.android.gms:play-services-location:21.0.1

### Image Loading
- io.coil-kt:coil-compose:2.5.0

## Build Configuration

- **Namespace**: `com.example.inkscape`
- **Compile SDK**: 35
- **Min SDK**: 26
- **Target SDK**: 35
- **Java Version**: 11

## Getting Started

1. Clone the repository
2. Set up Firebase project and add `google-services.json`
3. Configure Google Maps API key
4. Set up Supabase backend connection
5. Build and run the application

## Current Development Status

- ✅ Basic UI structure with Compose
- ✅ Firebase integration setup
- ✅ Authentication flow
- ✅ Location services integration
- ✅ Artist registration flow
- 🔄 Supabase backend integration
- 🔄 Search functionality implementation
- 🔄 Real-time data synchronization

## Architecture

The app follows modern Android development practices:
- **MVVM Architecture** with Compose
- **Single Activity** with Compose Navigation
- **State Management** using Compose state
- **Modular Components** for reusability
- **Firebase Integration** for backend services
