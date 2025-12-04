# GeoAssist - Municipal Public Services Locator

**GeoAssist** is a fully functional Android prototype application that
allows citizens to locate nearby hospitals, police stations, and
libraries using OpenStreetMap. The app is designed to work reliably even
without an internet connection by caching map tiles and POI data
locally.

This project strictly follows all assignment constraints:

-   Mapping: **OSMdroid** (OpenStreetMap) only -- no Google Maps SDK\
-   Location: Pure Android Location APIs\
-   Persistence: **Room** database only\
-   Networking: **Volley**\
-   UI: **Jetpack Compose** + Material Design 3\
-   Architecture: **MVVM**\
-   Language: **Kotlin**

## Features Implemented (All Mandatory)

-   Interactive OpenStreetMap with zoom, scroll, rotation gestures,
    compass, and scale bar\
-   Real-time user location display with accuracy circle\
-   Runtime location permission handling (with fallback to last known
    location)\
-   POI loading for hospitals, police stations, and libraries via
    Overpass API\
-   Local caching of all POIs using Room SQLite database\
-   Dynamic category filtering (toggle Hospital / Police / Library
    on/off)\
-   Distance calculation to each POI (meters or kilometers) using the
    Haversine formula\
-   Full offline mode support (map tiles + POIs)\
-   Offline warning banner when no internet is available\
-   Marker tap shows detailed dialog with name, category, address (if
    available), and distance\
-   Center-map-on-user FloatingActionButton\
-   Clean MVVM architecture with Repository pattern

## Architecture (MVVM)

    UI Layer (Jetpack Compose)
            ↓
       MainViewModel
            ↓
       PlaceRepository
            ↓
    ────────────────────────────
    │                          │
    Room DAO            Volley + Overpass API
    │                          │
    AppDatabase          Remote data source

## Technologies & Libraries

-   **Mapping**: `org.osmdroid:osmdroid-android:6.1.18`\
-   **Persistence**: `androidx.room:room-ktx:2.6.1`\
-   **Networking**: `com.android.volley:volley:1.2.1`\
-   **UI**: Jetpack Compose + Material3\
-   **Permissions**:
    `com.google.accompanist:accompanist-permissions:0.35.0-alpha`\
-   **Coroutines & Lifecycle**: Kotlin Coroutines + ViewModel Compose

All dependencies are declared in `app/build.gradle.kts` using the
`libs.versions.toml` format.

## Setup Instructions

1.  Clone the repository:

    ``` bash
    git clone https://github.com/MuhammadFaiz2001/GeoAssist.git
    ```

2.  Open the project in **Android Studio (Koala or later)**\

3.  Allow Gradle to sync the project\

4.  Connect a device or use an emulator with location enabled\

5.  Run the application\

6.  Grant **location permission** when prompted\

7.  On first launch (with internet), the app:

    -   Loads map tiles\
    -   Fetches nearby POIs\

8.  Subsequent launches work **fully offline** thanks to cached tiles +
    POIs

> **Note:** OSMdroid automatically caches map tiles in private storage.
> No storage permission required.


## Testing Scenarios Performed

-   GPS disabled → user location hidden\
-   Internet disabled → offline mode using cached data\
-   Location permission denied → fallback messaging\
-   Screen rotation → state preserved\
-   Extreme map zoom levels\
-   Empty database on first launch → POIs fetched and saved\
-   App relaunch → cached POIs load instantly

## Repository

GitHub: **https://github.com/MuhammadFaiz2001/GeoAssist**
