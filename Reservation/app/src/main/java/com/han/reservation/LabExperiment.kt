package com.han.reservation

data class LabExperiment(
    var key: String = "",
    var title: String = "",
    var description: String = "",
    var badge: ExperimentBadge = ExperimentBadge.NONE,
    var phase: ExperimentPhase = ExperimentPhase.EXPERIMENT
)

enum class ExperimentBadge {
    NONE,
    NEW,
    BETA
}