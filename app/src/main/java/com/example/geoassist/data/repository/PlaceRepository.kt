package com.example.geoassist.data.repository

import com.example.geoassist.data.local.PlaceDao
import com.example.geoassist.data.local.PlaceEntity
import com.example.geoassist.data.remote.OverpassService
import kotlinx.coroutines.flow.Flow

class PlaceRepository(
    private val placeDao: PlaceDao,
    private val overpassService: OverpassService
) {
    val allPlaces: Flow<List<PlaceEntity>> = placeDao.getAllPlaces()

    suspend fun refreshPlaces(bounds: DoubleArray) { // [minLat, minLon, maxLat, maxLon]
        try {
            val places = overpassService.fetchPlaces(bounds[0], bounds[1], bounds[2], bounds[3])
            placeDao.deleteAll()
            placeDao.insertAll(places)
        } catch (e: Exception) {
            // Offline: just keep cached data
        }
    }
}