package com.example.mindwalk.data

/**
 * Lightweight geographic coordinate used throughout the service and data layers.
 *
 * [Point] is a plain data transfer object that decouples the internal route representation
 * from OSMDroid's [org.osmdroid.util.GeoPoint]. Conversion to [org.osmdroid.util.GeoPoint]
 * happens only at the ViewModel boundary so that the data and service layers remain
 * independent of the mapping library.
 *
 * @property lat Latitude in decimal degrees (WGS 84).
 * @property lon Longitude in decimal degrees (WGS 84).
 */
data class Point(
    val lat: Double,
    val lon: Double
)
