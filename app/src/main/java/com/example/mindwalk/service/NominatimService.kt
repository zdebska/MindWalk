package com.example.mindwalk.service

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Nominatim search result representing a single place from OpenStreetMap.
 *
 * Only fields relevant to city identification are mapped; extra fields returned by
 * the API are ignored by Gson.
 *
 * @property addresstype Nominatim classification (e.g. `"city"`, `"town"`, `"village"`).
 * @property address     Structured address components used to extract city and country names.
 */
private data class NominatimPlace(
    @SerializedName("addresstype") val addresstype: String?,
    @SerializedName("address")     val address: NominatimAddress?
)

/**
 * Structured address components returned by Nominatim's `addressdetails=1` parameter.
 *
 * Fields are tried in order of population size until a non-null value is found.
 *
 * @property city         Name of a city-level settlement.
 * @property town         Name of a town-level settlement.
 * @property village      Name of a village.
 * @property municipality Name of a municipality.
 * @property county       Name of a county (fallback when no city/town/village is available).
 * @property country      English country name (guaranteed by `Accept-Language: en` header).
 */
private data class NominatimAddress(
    @SerializedName("city")         val city: String?,
    @SerializedName("town")         val town: String?,
    @SerializedName("village")      val village: String?,
    @SerializedName("municipality") val municipality: String?,
    @SerializedName("county")       val county: String?,
    @SerializedName("country")      val country: String?
)

/**
 * Client for the Nominatim OpenStreetMap geocoding API.
 *
 * Used during onboarding to provide city autocomplete suggestions as the user types.
 * Results are filtered to settlement-type address entries and formatted as
 * `"City, Country"` strings compatible with the OSMnx geocoder used by the backend.
 *
 * The `Accept-Language: en` header forces English country names regardless of the device
 * locale, which is required by the backend's Nominatim-based graph lookup.
 *
 * No API key is required; the `User-Agent: MindWalk/1.0` header is mandatory per
 * Nominatim's usage policy.
 */
class NominatimService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    /** Address types accepted as valid city suggestions. */
    private val cityTypes = setOf("city", "town", "village", "municipality", "borough")

    /**
     * Searches Nominatim for cities matching the given query string.
     *
     * Runs on [Dispatchers.IO]. Results are deduplicated and limited to 8 by the API
     * (`limit=8`). Only entries whose `addresstype` is in [cityTypes] are returned.
     *
     * @param query Partial city name typed by the user (minimum 2 characters recommended).
     * @return List of unique `"City, Country"` strings ready to display as autocomplete suggestions.
     *         Returns an empty list if the network call fails or yields no matching settlements.
     */
    suspend fun searchCity(query: String): List<String> = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val url = "https://nominatim.openstreetmap.org/search" +
                "?q=$encoded&format=json&addressdetails=1&limit=8"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "MindWalk/1.0")
            .header("Accept-Language", "en")
            .build()

        val body = client.newCall(request).execute().use { it.body?.string() }
            ?: return@withContext emptyList()

        val places: List<NominatimPlace> = gson.fromJson(
            body, object : TypeToken<List<NominatimPlace>>() {}.type
        ) ?: return@withContext emptyList()

        places
            .filter { it.addresstype in cityTypes }
            .mapNotNull { place ->
                val addr = place.address ?: return@mapNotNull null
                // Prefer the most specific settlement name available
                val city = addr.city ?: addr.town ?: addr.village
                    ?: addr.municipality ?: addr.county ?: return@mapNotNull null
                val country = addr.country ?: return@mapNotNull null
                "$city, $country"
            }
            .distinct()
    }
}
