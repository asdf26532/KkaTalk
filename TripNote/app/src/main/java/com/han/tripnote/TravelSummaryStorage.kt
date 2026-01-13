package com.han.tripnote

import android.content.Context

class TravelSummaryStorage(context: Context) {

    private val prefs =
        context.getSharedPreferences("travel_summary", Context.MODE_PRIVATE)

    fun save(summary: TravelSummary) {
        prefs.edit()
            .putString("title", summary.title)
            .putString("desc", summary.description)
            .apply()
    }

    fun load(): TravelSummary? {
        val title = prefs.getString("title", null) ?: return null
        val desc = prefs.getString("desc", null) ?: return null

        return TravelSummary(title, desc)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}