package com.example.mindwalk.data

import android.content.Context

/**
 * SharedPreferences wrapper for persisting the user's monthly goal.
 *
 * Stores two scalar values (goal type + target) rather than JSON so no serialiser
 * dependency is needed. Both values are written atomically via a single `apply()`.
 */
object GoalPreferences {

    private const val PREFS_NAME = "goal_prefs"
    private const val KEY_TYPE   = "goal_type"
    private const val KEY_TARGET = "goal_target"

    /**
     * Returns the stored [MonthlyGoal], or `null` if none has been set yet.
     *
     * @param context Any [Context]; used to access SharedPreferences.
     */
    fun getGoal(context: Context): MonthlyGoal? {
        val prefs  = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val type   = prefs.getString(KEY_TYPE, null) ?: return null
        val target = prefs.getInt(KEY_TARGET, 0)
        return MonthlyGoal(type, target)
    }

    /**
     * Persists [goal] to SharedPreferences.
     *
     * @param context Any [Context]; used to access SharedPreferences.
     * @param goal    The [MonthlyGoal] to store.
     */
    fun setGoal(context: Context, goal: MonthlyGoal) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TYPE,   goal.type)
            .putInt(KEY_TARGET,    goal.target)
            .apply()
    }
}
