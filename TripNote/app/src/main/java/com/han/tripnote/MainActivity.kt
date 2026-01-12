package com.han.tripnote

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.han.tripnote.databinding.ActivityMainBinding
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val todayPlaces = mutableListOf(
        TravelPlace("광안리", TravelType.SEA),
        TravelPlace("해운대", TravelType.SEA),
        TravelPlace("자갈치시장", TravelType.CITY)
    )

    private val travelDate = TravelDate(
        startDate = LocalDate.of(2026, 1, 5),
        endDate = LocalDate.of(2026, 1, 7)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateUI()

        binding.btnAddPlace.setOnClickListener {
            addPlace()
        }

        binding.btnRemovePlace.setOnClickListener {
            removePlace()
        }

        binding.cardSummary.setOnClickListener {
            showPlaceDetail()
        }

    }

    private fun updateUI() {
        val today = LocalDate.now()

        if (!travelDate.isTravelDay(today) || todayPlaces.isEmpty()) {
            binding.cardSummary.visibility = View.GONE
            binding.tvEmpty.visibility = View.VISIBLE
            binding.tvEmpty.text = "오늘은 여행 기간이 아니에요 ✨"
            return
        }

        val dayIndex = travelDate.dayIndex(today)

        if (todayPlaces.isEmpty()) {
            binding.cardSummary.visibility = View.GONE
            binding.tvEmpty.visibility = View.VISIBLE
            return
        }

        binding.cardSummary.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE

        binding.tvSummaryTitle.text = "부산 1일차 여행"
        binding.tvSummaryDesc.text =
            todayPlaces.joinToString(" → ") { it.name }
        binding.tvSummaryTime.text =
            "총 ${todayPlaces.size}곳 방문"

        binding.tvSummaryComment.text = generateTravelTypeSummary()
    }

    private fun showFinishedTravel() {
        val summary = TravelSummaryGenerator.generate(
            city = cityName,
            places = todayPlaces
        )

        binding.cardSummary.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE

        binding.tvSummaryTitle.text = summary.title
        binding.tvSummaryDesc.text = summary.description
        binding.tvSummaryTime.text =
            "총 ${todayPlaces.size}곳 방문"
        binding.tvSummaryComment.text =
            "이번 여행을 이렇게 기억해요"
    }

    private fun addPlace() {
        val index = todayPlaces.size + 1
        todayPlaces.add(
            TravelPlace(
                "새로운 장소 $index",
                TravelType.values().random()
            )
        )
        updateUI()
    }

    private fun removePlace() {
        if (todayPlaces.isNotEmpty()) {
            todayPlaces.removeAt(todayPlaces.lastIndex)
            updateUI()
        }
    }

    private fun generateTravelTypeSummary(): String {
        val seaCount = todayPlaces.count { it.type == TravelType.SEA }
        val cityCount = todayPlaces.count { it.type == TravelType.CITY }
        val natureCount = todayPlaces.count { it.type == TravelType.NATURE }

        return when {
            seaCount >= cityCount && seaCount >= natureCount ->
                "바다 중심의 여행이에요 🌊"
            cityCount >= natureCount ->
                "도시 위주의 여행이에요 🏙"
            else ->
                "자연을 즐기는 여행이에요 🌿"
        }
    }

    private fun showPlaceDetail() {
        val message = todayPlaces.joinToString("\n") {
            "• ${it.name} (${typeToText(it.type)})"
        }

        AlertDialog.Builder(this)
            .setTitle("오늘 방문한 장소")
            .setMessage(message)
            .setPositiveButton("확인", null)
            .show()
    }

    private fun typeToText(type: TravelType): String {
        return when (type) {
            TravelType.SEA -> "바다"
            TravelType.CITY -> "도시"
            TravelType.NATURE -> "자연"
        }
    }
}