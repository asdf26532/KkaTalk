package com.han.tripnote

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.han.tripnote.databinding.ActivityMainBinding
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var historyStorage: TravelHistoryStorage
    private lateinit var prefs: SharedPreferences

    private var selectedHistoryIndex: Int = -1
    private var filteredList: List<TravelHistory> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        historyStorage = TravelHistoryStorage(this)
        prefs = getSharedPreferences("favorite_prefs", MODE_PRIVATE)

        showStats()
        showBestTrip()
        showHistoryList(historyStorage.loadAll())


        binding.etSearchCity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterHistoryByCity(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        binding.btnHistoryDetail.setOnClickListener {
            showSelectedHistoryDetail()
        }

        binding.btnToggleFavorite.setOnClickListener {
            toggleFavorite()
        }

        binding.btnShareTrip.setOnClickListener {
            copyShareText()
        }

        binding.btnShareTrip.setOnClickListener {
            shareSelectedTrip()
        }

        binding.btnShareIntent.setOnClickListener {
            shareViaIntent()
        }
    }

    private fun showStats() {
        val list = historyStorage.loadAll()
        if (list.isEmpty()) {
            binding.cardStats.visibility = View.GONE
            return
        }

        binding.cardStats.visibility = View.VISIBLE

        val total = list.size
        val average = list.map { it.rating }.average()
        val mostVisitedCity = list.groupBy { it.city }
            .maxByOrNull { it.value.size }?.key ?: "-"
        val bestRatedCity = list.maxByOrNull { it.rating }?.city ?: "-"

        binding.tvTotalTrips.text = "총 여행 횟수: ${total}회"
        binding.tvAverageRating.text = "평균 만족도: ${"%.1f".format(average)} / 5"
        binding.tvMostVisitedCity.text = "가장 많이 간 도시: $mostVisitedCity"
        binding.tvBestRatedCity.text = "최고 만족 도시: $bestRatedCity"
    }

    private fun showHistoryList(list: List<TravelHistory>) {
        val favoriteId = prefs.getString("favorite_id", null)

        filteredList = list.sortedByDescending {
            it.id == favoriteId
        }

        if (filteredList.isEmpty()) {
            binding.tvHistoryList.text = "표시할 여행이 없어요"
            selectedHistoryIndex = -1
            return
        }

        binding.tvHistoryList.text = filteredList.mapIndexed { index, it ->
            val star = if (it.id == favoriteId) "⭐ " else ""
            "${index + 1}. $star${it.city} (${it.startDate} ~ ${it.endDate}) · ${it.rating}/5"
        }.joinToString("\n\n")

        binding.tvHistoryList.setOnClickListener {
            selectedHistoryIndex = 0
        }
    }

    private fun toggleFavorite() {
        val history = filteredList.getOrNull(selectedHistoryIndex)
            ?: return

        val current = prefs.getString("favorite_id", null)

        prefs.edit()
            .putString("favorite_id", if (current == history.id) null else history.id)
            .apply()

        showHistoryList(historyStorage.loadAll())
    }


    private fun filterHistoryByCity(keyword: String) {
        val all = historyStorage.loadAll()

        if (keyword.isBlank()) {
            showStats()
            showBestTrip()
            showHistoryList(all)
            return
        }

        val filtered = all.filter {
            it.city.contains(keyword, ignoreCase = true)
        }

        binding.cardStats.visibility = View.GONE
        binding.cardBestTrip.visibility = View.GONE
        showHistoryList(filtered)
    }

    private fun showSelectedHistoryDetail() {
        val history = filteredList.getOrNull(selectedHistoryIndex) ?: return

        AlertDialog.Builder(this)
            .setTitle("여행 상세")
            .setMessage(
                "도시: ${history.city}\n" +
                        "기간: ${history.startDate} ~ ${history.endDate}\n" +
                        "만족도: ${history.rating}/5"
            )
            .setPositiveButton("확인", null)
            .show()
    }

    private fun shareSelectedTrip() {
        val history = filteredList.getOrNull(selectedHistoryIndex)
            ?: run {
                Toast.makeText(this, "공유할 여행을 선택해주세요", Toast.LENGTH_SHORT).show()
                return
            }

        val shareText =
            "✈️ ${history.city} 여행 다녀왔어요!\n" +
                    "📅 ${history.startDate} ~ ${history.endDate}\n" +
                    "⭐ 만족도 ${history.rating}/5\n" +
                    "다음 여행도 기대 중!"

        val clipboard =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        clipboard.setPrimaryClip(
            ClipData.newPlainText("trip_share", shareText)
        )

        Toast.makeText(this, "여행 공유 문구가 복사됐어요", Toast.LENGTH_SHORT).show()
    }

    private fun buildShareText(history: TravelHistory): String {
        return "✈️ ${history.city} 여행 다녀왔어요!\n" +
                "📅 ${history.startDate} ~ ${history.endDate}\n" +
                "⭐ 만족도 ${history.rating}/5\n" +
                "다음 여행도 기대 중!"
    }

    private fun copyShareText() {
        val history = filteredList.getOrNull(selectedHistoryIndex)
            ?: run {
                Toast.makeText(this, "공유할 여행을 선택해주세요", Toast.LENGTH_SHORT).show()
                return
            }

        val clipboard =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        clipboard.setPrimaryClip(
            ClipData.newPlainText("trip_share", buildShareText(history))
        )

        Toast.makeText(this, "여행 공유 문구가 복사됐어요", Toast.LENGTH_SHORT).show()
    }

    private fun shareViaIntent() {
        val history = filteredList.getOrNull(selectedHistoryIndex)
            ?: run {
                Toast.makeText(this, "공유할 여행을 선택해주세요", Toast.LENGTH_SHORT).show()
                return
            }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, buildShareText(history))
        }

        startActivity(Intent.createChooser(intent, "여행 공유하기"))
    }

    private fun showBestTrip() {
        val list = historyStorage.loadAll()
        if (list.isEmpty()) return

        val best = list.maxByOrNull { it.rating } ?: return
        if (best.rating <= 0) return

        binding.cardBestTrip.visibility = View.VISIBLE
        binding.tvBestTripTitle.text = "⭐ 최고의 여행"
        binding.tvBestTripDesc.text =
            "${best.city} (${best.startDate} ~ ${best.endDate})"
        binding.tvBestTripRating.text =
            "만족도 ${best.rating} / 5"
    }
}