package com.example.geoassist.data.remote

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import android.content.Context
import com.example.geoassist.data.local.PlaceEntity
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class OverpassService(context: Context) {
    private val queue: RequestQueue = Volley.newRequestQueue(context)

    // Query for hospitals, police, libraries around a bounding box
    private fun buildOverpassQuery(swLat: Double, swLon: Double, neLat: Double, neLon: Double): String {
        val bbox = "$swLat,$swLon,$neLat,$neLon"
        return """
            [out:json];
            (
              node["amenity"="hospital"]($bbox);
              node["amenity"="police"]($bbox);
              node["amenity"="library"]($bbox);
            );
            out body;
        """.trimIndent()
    }

    suspend fun fetchPlaces(swLat: Double, swLon: Double, neLat: Double, neLon: Double): List<PlaceEntity> = suspendCoroutine { cont ->
        val query = buildOverpassQuery(swLat, swLon, neLat, neLon)
        val url = "https://overpass-api.de/api/interpreter?data=" + android.net.Uri.encode(query)

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val places = parseOverpassResponse(response)
                cont.resume(places)
            },
            { error ->
                cont.resumeWithException(error)
            })
        queue.add(request)
    }

    private fun parseOverpassResponse(json: JSONObject): List<PlaceEntity> {
        val elements = json.getJSONArray("elements")
        val list = mutableListOf<PlaceEntity>()
        for (i in 0 until elements.length()) {
            val el = elements.getJSONObject(i)
            val tags = el.optJSONObject("tags") ?: continue
            val name = tags.optString("name", "Unnamed")
            val amenity = tags.optString("amenity", "")
            val category = when (amenity) {
                "hospital" -> "hospital"
                "police" -> "police"
                "library" -> "library"
                else -> continue
            }
            val lat = el.getDouble("lat")
            val lon = el.getDouble("lon")
            val address = tags.optString("addr:full", tags.optString("addr:street", null))
            list.add(PlaceEntity(name = name, category = category, latitude = lat, longitude = lon, address = address))
        }
        return list
    }
}