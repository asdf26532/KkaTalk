package com.han.tripnote

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.han.tripnote.databinding.ActivityMainBinding
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val cityName = "부산"

    private val todayPlaces = mutableListOf(
        TravelPlace("광안리", TravelType.SEA),
        TravelPlace("해운대", TravelType.SEA),
        TravelPlace("자갈치시장", TravelType.CITY),
        TravelPlace("송도해수욕장", TravelType.SEA),
        TravelPlace("흰여울문화마을", TravelType.CITY)
    )

    private lateinit var summaryStorage: TravelSummaryStorage

    private var travelDate = TravelDate(
        startDate = LocalDate.of(2026, 1, 5),
        endDate = LocalDate.of(2026, 1, 7)
    )

    private val memoPrefs by lazy {
        getSharedPreferences("travel_memo", Context.MODE_PRIVATE)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        summaryStorage = TravelSummaryStorage(this)

        restoreMemo()
        updateUI()

        binding.btnAddPlace.setOnClickListener { addPlace() }
        binding.btnRemovePlace.setOnClickListener { removePlace() }
        binding.cardSummary.setOnClickListener { showPlaceDetail() }
        binding.btnNewTravel.setOnClickListener { startNewTravel() }
        binding.btnSaveMemo.setOnClickListener { saveMemo() }

    }

    private fun updateUI() {

        if (restoreLastSummaryIfExists()) {
            return
        }

        val today = LocalDate.now()
        val status = travelDate.status(today)

        when (status) {
            TravelStatus.BEFORE ->
                showMessage("여행이 아직 시작되지 않았어요 ✈️")

            TravelStatus.ONGOING ->
                showOngoingTravel(today)

            TravelStatus.FINISHED ->
                showFinishedTravel()
        }
    }

    private fun saveMemo() {
        val memo = binding.etTravelMemo.text.toString()
        memoPrefs.edit().putString("today_memo", memo).apply()
    }

    private fun restoreMemo() {
        binding.etTravelMemo.setText(
            memoPrefs.getString("today_memo", "")
        )
    }

    private fun startNewTravel() {
        summaryStorage.clear()
        todayPlaces.clear()
        memoPrefs.edit().clear().apply()

        travelDate = TravelDate(
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(2)
        )

        binding.etTravelMemo.setText("")
        updateUI()
    }

    private fun showMessage(text: String) {
        binding.cardSummary.visibility = View.GONE
        binding.tvEmpty.visibility = View.VISIBLE
        binding.tvEmpty.text = text
    }

    private fun showOngoingTravel(today: LocalDate) {
        if (todayPlaces.isEmpty()) {
            showMessage("오늘은 아직 방문한 장소가 없어요")
            return
        }

        binding.cardSummary.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE

        binding.tvSummaryTitle.text =
            "$cityName ${travelDate.dayIndex(today)}일차 여행"

        binding.tvSummaryDesc.text =
            todayPlaces.joinToString(" → ") { it.name }

        binding.tvSummaryTime.text =
            "총 ${todayPlaces.size}곳 방문"

        binding.tvSummaryComment.text =
            generateTravelTypeSummary()
    }

    private fun showFinishedTravel() {
        val summary = TravelSummaryGenerator.generate(
            city = cityName,
            places = todayPlaces
        )

        summaryStorage.save(summary)

        binding.cardSummary.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE

        binding.tvSummaryTitle.text = summary.title
        binding.tvSummaryDesc.text = summary.description
        binding.tvSummaryTime.text =
            "총 ${todayPlaces.size}곳 방문"
        binding.tvSummaryComment.text =
            "이번 여행을 이렇게 기억해요"
    }

    private fun restoreLastSummaryIfExists(): Boolean {
        val summary = summaryStorage.load() ?: return false

        binding.cardSummary.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE

        binding.tvSummaryTitle.text = summary.title
        binding.tvSummaryDesc.text = summary.description
        binding.tvSummaryTime.text = ""
        binding.tvSummaryComment.text = "지난 여행 요약이에요"

        return true
    }

    private fun addPlace() {
        todayPlaces.add(
            TravelPlace(
                "새로운 장소 ${todayPlaces.size + 1}",
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
        val sea = todayPlaces.count { it.type == TravelType.SEA }
        val city = todayPlaces.count { it.type == TravelType.CITY }
        val nature = todayPlaces.count { it.type == TravelType.NATURE }

        return when {
            sea >= city && sea >= nature -> "바다 중심의 여행이에요 🌊"
            city >= nature -> "도시 위주의 여행이에요 🏙"
            else -> "자연을 즐기는 여행이에요 🌿"
        }
    }

    private fun showPlaceDetail() {
        val message = todayPlaces.joinToString("\n") {
            "• ${it.name} (${typeToText(it.type)})"
        }

        AlertDialog.Builder(this)
            .setTitle("이번 여행 장소")
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