package com.han.reservation

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object LabExperimentRunner {

    private const val PREFS_NAME = "lab_prefs"

    fun runIfEnabled(
        context: Context,
        experimentKey: String,
        action: () -> Unit
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val enabled = prefs.getBoolean(experimentKey, false)
        val used = prefs.getBoolean("${experimentKey}_used", false)

        if (!enabled) {
            Log.d("LabRunner", "$experimentKey disabled")
            return
        }

        if (used) {
            Log.d("LabRunner", "$experimentKey already used")
            return
        }

        // 실험 실행
        action.invoke()

        // 사용 처리
        prefs.edit()
            .putBoolean("${experimentKey}_used", true)
            .apply()

        Log.d("LabRunner", "$experimentKey executed")
    }
}