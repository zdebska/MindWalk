package com.example.mindwalk.data

/**
 * Represents the user's self-set monthly walking goal.
 *
 * @property type   The metric being tracked: `"walks"`, `"distance"`, or `"time"`.
 * @property target The numeric target: walk count, kilometres (Int), or minutes.
 */
data class MonthlyGoal(val type: String, val target: Int)
