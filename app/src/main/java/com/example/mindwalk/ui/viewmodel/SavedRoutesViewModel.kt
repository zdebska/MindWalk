package com.example.mindwalk.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindwalk.data.RouteRepository
import com.example.mindwalk.data.SavedRoute
import kotlinx.coroutines.launch

class SavedRoutesViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = RouteRepository(app.applicationContext)

    var routes by mutableStateOf<List<SavedRoute>>(emptyList())
        private set

    init { reload() }

    fun reload() {
        viewModelScope.launch {
            routes = repository.getAll()
        }
    }

    fun save(route: SavedRoute) {
        viewModelScope.launch {
            repository.save(route)
            routes = repository.getAll()
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            repository.delete(id)
            routes = repository.getAll()
        }
    }
}
