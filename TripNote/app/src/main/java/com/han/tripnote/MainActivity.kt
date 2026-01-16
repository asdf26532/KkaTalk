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

    private var travelDate = TravelDate(
        startDate = LocalDate.of(2026, 1, 5),
        endDate = LocalDate.of(2026, 1, 7)
    )

    private val todayPlaces = mutableListOf(
        TravelPlace("광안리", TravelType.SEA),
        TravelPlace("해운대", TravelType.SEA)
    )

    private lateinit var summaryStorage: TravelSummaryStorage
    private lateinit var historyStorage: TravelHistoryStorage

    private val memoPrefs by lazy {
        getSharedPreferences("travel_memo", Context.MODE_PRIVATE)
    }

    private val ratingPrefs by lazy {
        getSharedPreferences("travel_rating", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        summaryStorage = TravelSummaryStorage(this)
        historyStorage = TravelHistoryStorage(this)

        restoreMemo()
        restoreRating()
        showHistory()
        updateUI()

        binding.btnAddPlace.setOnClickListener { addPlace() }
        binding.btnRemovePlace.setOnClickListener { removePlace() }
        binding.btnNewTravel.setOnClickListener { startNewTravel() }
        binding.btnSaveMemo.setOnClickListener { saveMemo() }

        binding.ratingTravel.setOnRatingBarChangeListener { _, rating, _ ->
            saveRating(rating)
            updateRatingText(rating)
        }

        binding.btnHistoryDetail.setOnClickListener {
            showLatestHistoryDetail()
        }

    }

    private fun updateUI() {
        if (restoreLastSummaryIfExists()) return

        val today = LocalDate.now()
        when (travelDate.status(today)) {
            TravelStatus.BEFORE -> showMessage("새 여행을 시작해보세요 ✈️")
            TravelStatus.ONGOING -> showOngoingTravel(today)
            TravelStatus.FINISHED -> showFinishedTravel()
        }
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

        val rating = binding.ratingTravel.rating.toInt()
        val ratingText = if (rating == 0) "" else " · 만족도 $rating/5"

        binding.tvSummaryComment.text =
            generateTravelTypeSummary() + ratingText
    }

    private fun showFinishedTravel() {
        saveHistoryIfFinished()

        val summary = TravelSummaryGenerator.generate(
            city = cityName,
            places = todayPlaces
        )

        summaryStorage.save(summary)

        binding.cardSummary.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE

        binding.tvSummaryTitle.text = summary.title
        binding.tvSummaryDesc.text = summary.description
        binding.tvSummaryComment.text = "이번 여행을 이렇게 기억해요"
    }

    private fun restoreLastSummaryIfExists(): Boolean {
        val summary = summaryStorage.load() ?: return false

        binding.cardSummary.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE

        binding.tvSummaryTitle.text = summary.title
        binding.tvSummaryDesc.text = summary.description
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

    private fun saveMemo() {
        memoPrefs.edit()
            .putString("today_memo", binding.etTravelMemo.text.toString())
            .apply()
    }

    private fun restoreMemo() {
        binding.etTravelMemo.setText(
            memoPrefs.getString("today_memo", "")
        )
    }

    private fun saveRating(rating: Float) {
        ratingPrefs.edit().putFloat("today_rating", rating).apply()
    }

    private fun restoreRating() {
        val rating = ratingPrefs.getFloat("today_rating", 0f)
        binding.ratingTravel.rating = rating
        updateRatingText(rating)
    }

    private fun updateRatingText(rating: Float) {
        binding.tvRatingText.text =
            if (rating == 0f) "아직 평가하지 않았어요"
            else "만족도 ${rating.toInt()} / 5"
    }

    private fun startNewTravel() {
        summaryStorage.clear()
        todayPlaces.clear()
        memoPrefs.edit().clear().apply()
        ratingPrefs.edit().clear().apply()

        travelDate = TravelDate(
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(2)
        )

        binding.etTravelMemo.setText("")
        binding.ratingTravel.rating = 0f
        updateRatingText(0f)
        showHistory()
        updateUI()
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

    private fun saveHistoryIfFinished() {
        val history = TravelHistory(
            city = cityName,
            startDate = travelDate.startDate.toString(),
            endDate = travelDate.endDate.toString(),
            rating = binding.ratingTravel.rating.toInt()
        )
        historyStorage.save(history)
        showHistory()
    }

    private fun showHistory() {
        val list = historyStorage.loadAll()

        binding.tvHistory.text =
            if (list.isEmpty()) {
                "아직 여행 기록이 없어요"
            } else {
                list.joinToString("\n\n") {
                    "${it.city} (${it.startDate} ~ ${it.endDate})\n만족도 ${it.rating}/5"
                }
            }
    }

    private fun showLatestHistoryDetail() {
        val history = historyStorage.loadAll().firstOrNull() ?: return

        AlertDialog.Builder(this)
            .setTitle("최근 여행 상세")
            .setMessage(
                "도시: ${history.city}\n" +
                        "기간: ${history.startDate} ~ ${history.endDate}\n" +
                        "만족도: ${history.rating}/5"
            )
            .setPositiveButton("확인", null)
            .show()
    }
}