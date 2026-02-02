package com.han.tripnote.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.han.tripnote.data.model.Trip

object TripStorage {

    private const val PREF_NAME = "trip_pref"
    private const val KEY_TRIP_LIST = "trip_list"

    private val gson = Gson()

    // 여행 목록 저장
    fun save(context: Context, tripList: List<Trip>) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(tripList)

        pref.edit().putString(KEY_TRIP_LIST, json).apply()
    }

    // 여행 목록 불러오기
    fun load(context: Context): MutableList<Trip> {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = pref.getString(KEY_TRIP_LIST, null) ?: return mutableListOf()

        val type = object : TypeToken<MutableList<Trip>>() {}.type
        return gson.fromJson(json, type)
    }
}