package com.example.geoassist.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "places")
data class PlaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // "hospital", "police", "library"
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)