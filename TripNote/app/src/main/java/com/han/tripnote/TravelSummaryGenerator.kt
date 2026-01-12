package com.han.tripnote

object TravelSummaryGenerator {

    fun generate(
        city: String,
        places: List<TravelPlace>
    ): TravelSummary {

        val total = places.size

        val mainType = places
            .groupingBy { it.type }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key ?: TravelType.CITY

        val title = when (mainType) {
            TravelType.SEA -> "$city 바다 여행"
            TravelType.CITY -> "$city 도심 여행"
            TravelType.NATURE -> "$city 자연 여행"
        }

        val description = when {
            total <= 2 ->
                "짧지만 인상 깊은 여행이었어요"
            total <= 5 ->
                "여유와 관광을 모두 즐긴 여행이었어요"
            else ->
                "알차게 꽉 찬 일정의 여행이었어요"
        }

        return TravelSummary(
            title = title,
            description = description
        )
    }
}