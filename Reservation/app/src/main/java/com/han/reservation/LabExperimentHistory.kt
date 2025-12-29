package com.han.reservation

import android.content.Context

object LabExperimentHistory {

    private const val PREFS_NAME = "lab_prefs"

    fun record(context: Context, key: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()

        val countKey = "${key}_count"
        val firstKey = "${key}_first_at"
        val lastKey = "${key}_last_at"

        val count = prefs.getInt(countKey, 0)

        val editor = prefs.edit()
        editor.putInt(countKey, count + 1)
        editor.putLong(lastKey, now)

        if (count == 0) {
            editor.putLong(firstKey, now)
        }

        editor.apply()
    }

    fun getCount(context: Context, key: String): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("${key}_count", 0)
    }
}