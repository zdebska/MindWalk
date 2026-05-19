package com.example.mindwalk.data

import com.google.gson.annotations.SerializedName

/**
 * Request body sent to `POST /route` to generate a walking route on the Azure backend.
 *
 * The backend is a Python service that uses OSMnx to find a mindful walking route on the
 * pre-computed street graph for the given city. Fields use snake_case names via
 * [SerializedName] to match the API's JSON contract.
 *
 * @property place        City name in Nominatim-compatible format, e.g. `"Brno, Czechia"`.
 * @property distanceKm   Desired route length in kilometres. Derived from duration at 1 km per 12 min.
 * @property startLat     Optional starting latitude. If null, the backend chooses a random node.
 * @property startLon     Optional starting longitude. If null, the backend chooses a random node.
 * @property endLat       Optional end latitude for point-to-point (Line) routes.
 * @property endLon       Optional end longitude for point-to-point (Line) routes.
 * @property mode         Routing preference: `"green"` (parks), `"quiet"` (low traffic), `"sight"` (landmarks).
 * @property seed         Deterministic seed for route selection. Incrementing this value produces
 *                        a different route with the same other parameters, used by the "New route" feature.
 * @property nCandidates  Number of candidate routes the backend generates before selecting the best one.
 * @property beamWidth    Beam width parameter for the backend's beam-search route planner.
 */
data class RouteRequest(
    val place: String,
    @SerializedName("distance_km") val distanceKm: Double,
    @SerializedName("start_lat") val startLat: Double? = null,
    @SerializedName("start_lon") val startLon: Double? = null,
    @SerializedName("end_lat") val endLat: Double? = null,
    @SerializedName("end_lon") val endLon: Double? = null,
    val mode: String = "green",
    val seed: Int = 0,
    @SerializedName("n_candidates") val nCandidates: Int = 5,
    @SerializedName("beam_width") val beamWidth: Int = 3
)

/**
 * GeoJSON LineString geometry as returned inside [RouteResponse].
 *
 * Coordinates are in [longitude, latitude] order, following the GeoJSON specification
 * (RFC 7946). The [com.example.mindwalk.service.PythonRouteService] swaps them to
 * [latitude, longitude] when converting to [Point] objects.
 *
 * @property type        Always `"LineString"` for a route polyline.
 * @property coordinates Ordered list of [lon, lat] pairs representing the route path.
 */
data class GeoJsonLineString(
    val type: String,
    val coordinates: List<List<Double>>
)

/**
 * Response body returned by `POST /route`.
 *
 * @property lengthKm     Actual route length in kilometres as computed by the backend.
 * @property geojson      GeoJSON LineString containing the ordered coordinate sequence of the route.
 * @property qualityScore Optional score assigned by the backend reflecting route quality
 *                        (e.g. proportion of green space). Present in some backend versions.
 */
data class RouteResponse(
    @SerializedName("length_km") val lengthKm: Double,
    val geojson: GeoJsonLineString,
    @SerializedName("quality_score") val qualityScore: Double? = null
)

/**
 * In-memory summary of a generated route, used to pass planning metadata between screens.
 *
 * @property durationMin       Planned walk duration in minutes.
 * @property mindfulnessMode   Mindfulness mode label as shown in the UI.
 * @property routeShape        Route shape label as shown in the UI ("Loop" or "Line").
 * @property distanceKm        Route length in kilometres.
 * @property points            Ordered list of geographic coordinates forming the route.
 */
data class RoutePreviewData(
    val durationMin: Int,
    val mindfulnessMode: String,
    val routeShape: String,
    val distanceKm: Double,
    val points: List<Point>
)
