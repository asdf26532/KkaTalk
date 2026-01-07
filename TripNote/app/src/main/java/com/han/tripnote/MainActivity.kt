package com.han.tripnote

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.han.tripnote.databinding.ActivityMainBinding
import java.time.LocalDate

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val summaries = createMockSummaries()

        val todaySummary = summaries.find { it.date == LocalDate.now() }

        if (todaySummary != null) {
            showSummary(todaySummary)
        } else {
            showEmptyState()
        }

    }

    private fun showSummary(summary: TodayTravelSummary) {
        binding.cardSummary.visibility = android.view.View.VISIBLE
        binding.tvEmpty.visibility = android.view.View.GONE

        binding.tvSummaryTitle.text =
            "${summary.city} ${summary.dayIndex}일차 여행"

        binding.tvSummaryDesc.text =
            summary.places.joinToString(" → ") { it.name }

        binding.tvSummaryTime.text =
            "총 ${summary.places.size}곳 방문"

        binding.tvSummaryComment.text =
            generateSummaryComment(summary.places)
    }

    private fun showEmptyState() {
        binding.cardSummary.visibility = android.view.View.GONE
        binding.tvEmpty.visibility = android.view.View.VISIBLE
    }

    private fun generateSummaryComment(places: List<TravelPlace>): String {
        val seaCount = places.count { it.category == "바다" }

        return when {
            seaCount >= 2 -> "오늘은 바다 중심의 여행이었어요"
            places.size >= 4 -> "이동이 많은 일정이었어요"
            else -> "여유로운 하루였어요"
        }
    }

    private fun createMockSummaries(): List<TodayTravelSummary> {
        return listOf(
            TodayTravelSummary(
                city = "부산",
                dayIndex = 1,
                date = LocalDate.now(),
                places = listOf(
                    TravelPlace("광안리", "바다"),
                    TravelPlace("해운대", "바다"),
                    TravelPlace("자갈치시장", "시장")
                )
            ),
            TodayTravelSummary(
                city = "서울",
                dayIndex = 2,
                date = LocalDate.now().minusDays(1),
                places = listOf(
                    TravelPlace("경복궁", "관광"),
                    TravelPlace("북촌", "관광")
                )
            )
        )
    }
}