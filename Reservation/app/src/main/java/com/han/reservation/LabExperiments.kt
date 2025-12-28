package com.han.reservation

object LabExperiments {

    val experiments = listOf(
        LabExperiment(
            key = LabKeys.QUICK_RESERVE,
            title = "빠른 예약 입력",
            description = "예약 화면에서 기본 메시지를 자동으로 채워줍니다.",
            badge = ExperimentBadge.BETA,
            phase = ExperimentPhase.EXPERIMENT
        )
    )

    fun visibleExperiments(): List<LabExperiment> {
        return experiments.filter { it.phase == ExperimentPhase.EXPERIMENT }
    }

    fun find(key: String): LabExperiment {
        return experiments.first { it.key == key }
    }

}