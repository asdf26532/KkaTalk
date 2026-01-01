package com.han.reservation

import android.content.Context

object LabPrefs {

    private const val PREF_NAME = "lab_prefs"

    fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun isEnabled(context: Context, key: String): Boolean =
        prefs(context).getBoolean(key, false)

    fun setEnabled(context: Context, key: String, enabled: Boolean) {
        prefs(context).edit().putBoolean(key, enabled).apply()
    }

    fun increaseRunCount(context: Context, key: String) {
        val count = prefs(context).getInt("${key}_count", 0)
        prefs(context).edit().putInt("${key}_count", count + 1).apply()
    }
}