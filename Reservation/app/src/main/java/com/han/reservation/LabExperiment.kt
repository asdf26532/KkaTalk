package com.han.reservation

data class LabExperiment(
    var key: String = "",
    var title: String = "",
    var description: String = ""
)

enum class ExperimentBadge {
    NONE,
    NEW,
    BETA
}