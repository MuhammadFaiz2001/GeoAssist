package com.example.geoassist.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.geoassist.di.DatabaseModule
import com.example.geoassist.data.remote.OverpassService
import com.example.geoassist.data.repository.PlaceRepository
import com.example.geoassist.ui.components.OsmMap
import com.example.geoassist.viewmodel.MainViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted
import android.Manifest
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.osmdroid.views.MapView

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen() {
    val database = DatabaseModule.provideDatabase(LocalContext.current)
    val repository = remember { PlaceRepository(database.placeDao(), OverpassService(LocalContext.current)) }
    val viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory(repository))

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    // Location updates (simplified - in real app use a LocationSource or FusedLocationProvider)
    // Here we assume you have a LocationManager in a service or use a library

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("GeoAssist") })
        },
        floatingActionButton = {
            Column {
                FloatingActionButton(onClick = { /* refresh */ }) {
                    Icon(Icons.Default.Refresh, "Refresh POIs")
                }
                Spacer(Modifier.height(8.dp))
                FloatingActionButton(onClick = { /* center on user */ }) {
                    Icon(Icons.Default.MyLocation, "Center map")
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            var mapView: MapView? by remember { mutableStateOf(null) }

            OsmMap(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize(),
                onMapReady = { map -> mapView = map }
            )

            // Filter chips
            Row(modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
                listOf("hospital", "police", "library").forEach { cat ->
                    FilterChip(
                        selected = cat in viewModel.selectedFilters.value,
                        onClick = { viewModel.toggleFilter(cat) },
                        label = { Text(cat.replaceFirstChar { it.uppercase() }) }
                    )
                    Spacer(Modifier.width(8.dp))
                }
            }

            // Offline banner
            if (viewModel.isOffline.value) {
                Snackbar(modifier = Modifier.align(Alignment.BottomCenter)) {
                    Text("Offline mode - showing cached data")
                }
            }

            // Marker detail dialog
            viewModel.selectedPlace.value?.let { place ->
                AlertDialog(
                    onDismissRequest = { viewModel.selectPlace(null) },
                    title = { Text(place.name) },
                    text = {
                        Column {
                            Text("Category: ${place.category.replaceFirstChar { it.uppercase() }}")
                            place.address?.let { Text("Address: $it") }
                            Text("Distance: ${viewModel.distanceToUser(place)}")
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { viewModel.selectPlace(null) }) { Text("Close") }
                    }
                )
            }
        }
    }
}