package com.example.geoassist.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geoassist.data.local.PlaceEntity
import com.example.geoassist.data.repository.PlaceRepository
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import android.location.Location
import kotlin.math.*

class MainViewModel(
    private val repository: PlaceRepository
) : ViewModel() {

    val places = repository.allPlaces

    private val _userLocation = mutableStateOf<GeoPoint?>(null)
    val userLocation: State<GeoPoint?> = _userLocation

    private val _selectedFilters = mutableStateOf(setOf("hospital", "police", "library"))
    val selectedFilters: State<Set<String>> = _selectedFilters

    private val _selectedPlace = mutableStateOf<PlaceEntity?>(null)
    val selectedPlace: State<PlaceEntity?> = _selectedPlace

    private val _isOffline = mutableStateOf(false)
    val isOffline: State<Boolean> = _isOffline

    fun updateUserLocation(location: Location?) {
        location?.let {
            _userLocation.value = GeoPoint(it.latitude, it.longitude)
        }
    }

    fun toggleFilter(category: String) {
        val set = _selectedFilters.value.toMutableSet()
        if (category in set) set.remove(category) else set.add(category)
        _selectedFilters.value = set
    }

    fun selectPlace(place: PlaceEntity?) {
        _selectedPlace.value = place
    }

    fun refreshPlaces(map: MapView) {
        viewModelScope.launch {
            val bounds = map.boundingBox
            val minLat = bounds.latSouth
            val minLon = bounds.lonWest
            val maxLat = bounds.latNorth
            val maxLon = bounds.lonEast
            try {
                repository.refreshPlaces(doubleArrayOf(minLat, minLon, maxLat, maxLon))
                _isOffline.value = false
            } catch (e: Exception) {
                _isOffline.value = true
            }
        }
    }

    // Haversine formula
    fun distanceToUser(place: PlaceEntity): String {
        val user = _userLocation.value ?: return "-"
        val lat1 = user.latitude
        val lon1 = user.longitude
        val lat2 = place.latitude
        val lon2 = place.longitude

        val R = 6371000.0 // meters
        val φ1 = Math.toRadians(lat1)
        val φ2 = Math.toRadians(lat2)
        val Δφ = Math.toRadians(lat2 - lat1)
        val Δλ = Math.toRadians(lon2 - lon1)

        val a = sin(Δφ / 2).pow(2) + cos(φ1) * cos(φ2) * sin(Δλ / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = R * c

        return if (distance >= 1000) {
            String.format("%.1f km", distance / 1000)
        } else {
            String.format("%.0f m", distance)
        }
    }
}