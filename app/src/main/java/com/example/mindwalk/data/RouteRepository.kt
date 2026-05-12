package com.example.mindwalk.data

import android.content.Context

class RouteRepository(context: Context) {
    private val dao = MindWalkDatabase.get(context).savedRouteDao()

    suspend fun getAll(): List<SavedRoute> = dao.getAll()

    suspend fun save(route: SavedRoute) = dao.insert(route)

    suspend fun delete(id: String) = dao.delete(id)
}
