package com.example.mindwalk.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindwalk.data.GoalPreferences
import com.example.mindwalk.data.MonthlyGoal
import com.example.mindwalk.data.RouteRepository
import com.example.mindwalk.data.WalkRecord
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ViewModel managing the complete walk history and the user's monthly goal.
 *
 * Exposes [walks] as Compose observable state so [com.example.mindwalk.ui.screens.JourneyScreen]
 * recomposes automatically when new walks are recorded. Current-month progress values
 * ([walksThisMonth], [distanceThisMonthKm], [timeThisMonthMin]) are computed lazily from [walks]
 * and used by the goal progress card.
 *
 * @param app Application context used to initialise [RouteRepository] and [GoalPreferences].
 */
class WalkHistoryViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = RouteRepository(app.applicationContext)

    /** All completed walk records, most-recent first. */
    var walks by mutableStateOf<List<WalkRecord>>(emptyList())
        private set

    /** The user's currently saved monthly goal, or null if none has been set. */
    var monthlyGoal by mutableStateOf<MonthlyGoal?>(null)
        private set

    init {
        reload()
        monthlyGoal = GoalPreferences.getGoal(app.applicationContext)
    }

    // ── Current-month progress ────────────────────────────────────────────────

    /** Number of walks completed in the current calendar month. */
    val walksThisMonth: Int get() = walks.count { isThisMonth(it.completedAt) }

    /** Total distance walked in the current calendar month, in kilometres. */
    val distanceThisMonthKm: Double get() =
        walks.filter { isThisMonth(it.completedAt) }.sumOf { it.distanceKm }

    /** Total time walked in the current calendar month, in minutes. */
    val timeThisMonthMin: Int get() =
        walks.filter { isThisMonth(it.completedAt) }.sumOf { it.durationMin }

    /** Most frequently used mindfulness mode across all walks, or "—" if no walks. */
    val popularMode: String get() =
        walks.groupingBy { it.mode }.eachCount().maxByOrNull { it.value }?.key ?: "—"

    private fun isThisMonth(timestampMs: Long): Boolean {
        val now = Calendar.getInstance()
        val c   = Calendar.getInstance().apply { timeInMillis = timestampMs }
        return c.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
               c.get(Calendar.YEAR)  == now.get(Calendar.YEAR)
    }

    // ── Database operations ───────────────────────────────────────────────────

    /** Reloads the full walk history from Room. Called each time JourneyScreen enters the composition. */
    fun reload() {
        viewModelScope.launch {
            walks = repository.getAllWalks()
        }
    }

    /**
     * Persists a new [WalkRecord] and immediately refreshes [walks].
     *
     * @param walk The completed walk session to record.
     */
    fun record(walk: WalkRecord) {
        viewModelScope.launch {
            repository.recordWalk(walk)
            walks = repository.getAllWalks()
        }
    }

    // ── Goal operations ───────────────────────────────────────────────────────

    /**
     * Saves [goal] to [GoalPreferences] and updates [monthlyGoal] state.
     *
     * @param goal The [MonthlyGoal] chosen by the user on [com.example.mindwalk.ui.screens.SetGoalScreen].
     */
    fun saveGoal(goal: MonthlyGoal) {
        GoalPreferences.setGoal(getApplication(), goal)
        monthlyGoal = goal
    }

    /** Reloads [monthlyGoal] from SharedPreferences (called on screen resume). */
    fun reloadGoal() {
        monthlyGoal = GoalPreferences.getGoal(getApplication())
    }
}
