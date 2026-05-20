package com.example.mindwalk.ui.util

import com.example.mindwalk.data.Point
import org.osmdroid.util.GeoPoint

/**
 * Converts this [Point] (the internal coordinate model used by the data and service layers)
 * to an OSMDroid [GeoPoint] required by map overlays and the [MapView] controller.
 */
fun Point.toGeoPoint(): GeoPoint = GeoPoint(lat, lon)

/**
 * Converts this OSMDroid [GeoPoint] back to the app's internal [Point] representation.
 *
 * Used when reading tap coordinates from the map in
 * [com.example.mindwalk.ui.screens.LocationPickerScreen].
 */
fun GeoPoint.toPoint(): Point = Point(latitude, longitude)
