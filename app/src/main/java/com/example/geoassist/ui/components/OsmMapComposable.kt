package com.example.geoassist.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import com.example.geoassist.data.local.PlaceEntity
import com.example.geoassist.viewmodel.MainViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OsmMap(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onMapReady: (MapView) -> Unit = {}
) {
    val context = LocalContext.current
    val places = viewModel.places.collectAsStateWithLifecycle(initialValue = emptyList())
    val filters = viewModel.selectedFilters.collectAsStateWithLifecycle()
    val userLocation = viewModel.userLocation.collectAsStateWithLifecycle()

    val map = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setBuiltInZoomControls(true)
            controller.setZoom(15.0)

            // Offline tile cache
            tileProvider.tileCache?.let { it.isProtected = true }

            // Compass, scale, rotation
            overlays.add(org.osmdroid.views.overlay.compass.CompassOverlay(context, this))
            overlays.add(org.osmdroid.views.overlay.ScaleBarOverlay(this))
            isTilesScaledToDpi = true
        }
    }

    AndroidView(
        factory = { map },
        modifier = modifier,
        update = {
            onMapReady(it)

            // User location overlay
            val myLocationOverlay = MyLocationNewOverlay(it)
            myLocationOverlay.enableMyLocation()
            myLocationOverlay.enableFollowLocation()
            it.overlays.add(myLocationOverlay)

            // Clear old markers
            it.overlays.removeAll { o -> o is Marker && o !is MyLocationNewOverlay }

            // Add filtered markers
            places.value.filter { p -> p.category in filters.value }.forEach { place ->
                val marker = Marker(it)
                marker.position = org.osmdroid.util.GeoPoint(place.latitude, place.longitude)
                marker.title = place.name
                marker.subDescription = "${viewModel.distanceToUser(place)} • ${place.category.replaceFirstChar { it.uppercase() }}"
                marker.setOnMarkerClickListener { _, _ ->
                    viewModel.selectPlace(place)
                    true
                }
                // Optional: different icons per category
                it.overlays.add(marker)
            }

            it.invalidate()
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            // Cleanup if needed
        }
    }
}