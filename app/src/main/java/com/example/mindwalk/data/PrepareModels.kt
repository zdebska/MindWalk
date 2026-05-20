package com.example.mindwalk.data

/**
 * Request body sent to `POST /prepare` to trigger city graph pre-computation on the backend.
 *
 * The backend uses OSMnx to download the street graph for the given city from OpenStreetMap
 * and pre-processes it for routing. This is required once per city before any route can be generated.
 *
 * @property place City name in Nominatim-compatible format, e.g. `"Brno, Czechia"`.
 */
data class PrepareRequest(val place: String)

/**
 * Response body returned by `POST /prepare`.
 *
 * Indicates that the preparation job was accepted. The [status] field reflects the initial
 * state of the job; the client must poll `GET /prepare/status` for completion.
 *
 * @property place  The city name echoed back by the server.
 * @property status Job status at submission time (typically `"pending"`).
 * @property nodes  Number of graph nodes after pre-processing, if already available.
 * @property edges  Number of graph edges after pre-processing, if already available.
 */
data class PrepareResponse(
    val place: String,
    val status: String,
    val nodes: Int? = null,
    val edges: Int? = null
)

/**
 * Response body returned by `GET /prepare/status?place=…`.
 *
 * The client polls this endpoint every 2 seconds until [status] is either `"ready"` or
 * `"error"`. See [com.example.mindwalk.service.PythonRouteService.pollPrepareStatus].
 *
 * @property status  One of `"pending"`, `"ready"`, or `"error"`.
 * @property message Human-readable progress description or error detail, shown to the user
 *                   during the onboarding loading phase.
 */
data class PrepareStatusResponse(
    val status: String,
    val message: String? = null
)
