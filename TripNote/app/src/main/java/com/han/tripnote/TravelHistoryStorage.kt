package com.han.tripnote

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class TravelHistoryStorage(context: Context) {

    private val prefs =
        context.getSharedPreferences("travel_history", Context.MODE_PRIVATE)

    fun save(history: TravelHistory) {
        val list = loadAll().toMutableList()
        list.add(0, history)

        val jsonArray = JSONArray()
        list.take(5).forEach {
            val obj = JSONObject()
            obj.put("city", it.city)
            obj.put("startDate", it.startDate)
            obj.put("endDate", it.endDate)
            obj.put("rating", it.rating)
            jsonArray.put(obj)
        }

        prefs.edit().putString("list", jsonArray.toString()).apply()
    }

    fun loadAll(): List<TravelHistory> {
        val json = prefs.getString("list", null) ?: return emptyList()
        val array = JSONArray(json)

        return List(array.length()) {
            val obj = array.getJSONObject(it)
            TravelHistory(
                city = obj.getString("city"),
                startDate = obj.getString("startDate"),
                endDate = obj.getString("endDate"),
                rating = obj.getInt("rating")
            )
        }
    }
}