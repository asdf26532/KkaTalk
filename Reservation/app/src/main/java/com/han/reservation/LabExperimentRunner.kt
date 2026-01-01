package com.han.reservation

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast

object LabExperimentRunner {

    fun run(
        context: Context,
        experiment: LabExperiment,
        action: () -> Unit
    ) {
        LabExperimentAutoCloser.checkAndClose(context, experiment)
        if (!shouldRun(context, experiment)) return

        try {
            action.invoke()
            LabPrefs.increaseRunCount(context, experiment.key)
        } catch (e: Exception) {
            disable(context, experiment)
        }
    }

    private fun shouldRun(context: Context, exp: LabExperiment): Boolean {
        return exp.phase == ExperimentPhase.EXPERIMENT &&
                LabPrefs.isEnabled(context, exp.key)
    }

    private fun disable(context: Context, exp: LabExperiment) {
        LabPrefs.setEnabled(context, exp.key, false)
    }
}