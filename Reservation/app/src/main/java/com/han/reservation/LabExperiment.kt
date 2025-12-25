package com.han.reservation

data class LabExperiment(
    val key: String,
    val title: String,
    val description: String,
    val badge: ExperimentBadge = ExperimentBadge.NONE
)

enum class ExperimentBadge {
    NONE,
    NEW,
    BETA
}