package com.example.mindwalk.ui.util

import com.example.mindwalk.data.Point
import org.osmdroid.util.GeoPoint

fun Point.toGeoPoint(): GeoPoint = GeoPoint(lat, lon)
fun GeoPoint.toPoint(): Point = Point(latitude, longitude)
