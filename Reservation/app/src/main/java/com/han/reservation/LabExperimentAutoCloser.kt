package com.han.reservation

object LabExperimentAutoCloser {

    fun checkAndClose(
        context: Context,
        experiment: LabExperiment
    ) {
        if (experiment.phase != ExperimentPhase.EXPERIMENT) return

        val prefs = context.getSharedPreferences("lab_prefs", Context.MODE_PRIVATE)

        experiment.endCondition?.let { condition ->

            // 실행 횟수 조건
            condition.maxRunCount?.let { max ->
                val count = prefs.getInt("${experiment.key}_count", 0)
                if (count >= max) {
                    experiment.phase = ExperimentPhase.GRADUATED
                    return
                }
            }

            // 기간 조건
            condition.maxDays?.let { days ->
                val firstAt = prefs.getLong("${experiment.key}_first_at", 0L)
                if (firstAt > 0) {
                    val passedDays =
                        (System.currentTimeMillis() - firstAt) / (1000 * 60 * 60 * 24)
                    if (passedDays >= days) {
                        experiment.phase = ExperimentPhase.REMOVED
                        return
                    }
                }
            }
        }
    }
}