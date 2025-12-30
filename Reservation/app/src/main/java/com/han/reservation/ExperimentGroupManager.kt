package com.han.reservation

import android.content.Context
import kotlin.random.Random

object ExperimentGroupManager {

    private const val PREFS_NAME = "lab_prefs"

    fun getGroup(context: Context, key: String): ExperimentGroup {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString("${key}_group", null)

        if (saved != null) {
            return ExperimentGroup.valueOf(saved)
        }

        val group = if (Random.nextBoolean()) {
            ExperimentGroup.A
        } else {
            ExperimentGroup.B
        }

        prefs.edit()
            .putString("${key}_group", group.name)
            .apply()

        return group
    }
}