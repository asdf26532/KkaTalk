package com.han.reservation

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast

object LabExperimentRunner {

    private const val PREFS_NAME = "lab_prefs"

    fun runSafely(
        context: Context,
        experiment: LabExperiment,
        action: () -> Unit
    ) {
        if (!shouldRun(context, experiment)) return

        try {
            action.invoke()

            // 정상 실행 → 사용 처리
            markUsed(context, experiment)

        } catch (e: Exception) {
            disableExperiment(context, experiment)
            showFailToast(context)
        }
    }

    private fun disableExperiment(
        context: Context,
        experiment: LabExperiment
    ) {
        val prefs = context.getSharedPreferences("lab_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(experiment.key, false)
            .putBoolean("${experiment.key}_used", false)
            .apply()
    }

    private fun showFailToast(context: Context) {
        Toast.makeText(
            context,
            "실험 기능에 문제가 발생하여 자동으로 비활성화되었습니다",
            Toast.LENGTH_LONG
        ).show()
    }

    fun shouldRun(
        context: Context,
        experiment: LabExperiment
    ): Boolean {
        return when (experiment.phase) {
            ExperimentPhase.GRADUATED -> true
            ExperimentPhase.REMOVED -> false
            ExperimentPhase.EXPERIMENT -> {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val enabled = prefs.getBoolean(experiment.key, false)
                val used = prefs.getBoolean("${experiment.key}_used", false)
                enabled && !used
            }
        }
    }

    fun markUsed(context: Context, experiment: LabExperiment) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("${experiment.key}_used", true)
            .apply()
    }



   /* fun runIfEnabled(
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
    }*/
}