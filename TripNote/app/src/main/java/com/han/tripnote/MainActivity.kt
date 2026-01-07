package com.han.tripnote

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.han.tripnote.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val summary = TodayTravelSummary(
            city = "부산",
            dayIndex = 1,
            places = listOf(
                TravelPlace("광안리", "바다"),
                TravelPlace("해운대", "바다"),
                TravelPlace("자갈치시장", "시장")
            )
        )

        bindSummary(summary)
    }

    private fun bindSummary(summary: TodayTravelSummary) {
        binding.tvSummaryTitle.text =
            "${summary.city} ${summary.dayIndex}일차 여행"

        binding.tvSummaryDesc.text =
            summary.places.joinToString(" → ") { it.name }

        binding.tvSummaryTime.text =
            "총 ${summary.places.size}곳 방문"

        binding.tvSummaryComment.text =
            generateSummaryComment(summary.places)
    }

    private fun generateSummaryComment(places: List<TravelPlace>): String {
        val seaCount = places.count { it.category == "바다" }

        return when {
            seaCount >= 2 -> "오늘은 바다 중심의 여행이었어요"
            places.size >= 4 -> "이동이 많은 일정이었어요"
            else -> "여유로운 하루였어요"
        }
    }
}