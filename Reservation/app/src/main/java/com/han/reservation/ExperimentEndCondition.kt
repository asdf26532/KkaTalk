package com.han.reservation

data class ExperimentEndCondition(
    var maxRunCount: Int? = null,   // n회 실행 후 종료
    var maxDays: Int? = null        // n일 후 종료
)