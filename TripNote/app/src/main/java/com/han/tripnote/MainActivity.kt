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
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.han.tripnote.databinding.ActivityMainBinding
import java.time.LocalDate
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences

    private val histories = mutableListOf<TravelHistory>()
    private var selected: TravelHistory? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("trip_prefs", MODE_PRIVATE)

        seedData()
        restoreLastSelected()
        renderList()
        updateStats()
        updateSelectedInfo()

        binding.etSearch.addTextChangedListener {
            renderList(it.toString())
        }

        binding.btnAddTrip.setOnClickListener {
            showAddTripDialog()
        }

        binding.tvHistoryList.setOnClickListener {
            selected = histories.lastOrNull()
            selected?.let {
                binding.etMemo.setText(it.memo)
                saveLastSelected(it.id)
            }
            updateSelectedInfo()
            Toast.makeText(this, "여행 선택됨", Toast.LENGTH_SHORT).show()
        }

        binding.btnFavorite.setOnClickListener {
            selected?.let {
                it.isFavorite = !it.isFavorite
                updateSelectedInfo()
                renderList()
            }
        }

        binding.btnCopy.setOnClickListener {
            selected?.let {
                val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(
                    ClipData.newPlainText(
                        "trip",
                        "${it.city} (${it.startDate}~${it.endDate})"
                    )
                )
                Toast.makeText(this, "복사됨", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnShare.setOnClickListener {
            selected?.let {
                startActivity(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "${it.city} 여행 (${it.startDate}~${it.endDate})"
                        )
                    }
                )
            }
        }

        binding.btnSaveMemo.setOnClickListener {
            selected?.memo = binding.etMemo.text.toString()
            Toast.makeText(this, "후기 저장됨", Toast.LENGTH_SHORT).show()
        }

    }

    private fun showAddTripDialog() {
        val city = EditText(this).apply { hint = "도시" }
        val start = EditText(this).apply { hint = "시작일 (YYYY-MM-DD)" }
        val end = EditText(this).apply { hint = "종료일 (YYYY-MM-DD)" }
        val rating = EditText(this).apply { hint = "평점 (1~5)" }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 0)
            addView(city)
            addView(start)
            addView(end)
            addView(rating)
        }

        AlertDialog.Builder(this)
            .setTitle("여행 추가")
            .setView(layout)
            .setPositiveButton("추가") { _, _ ->
                val trip = TravelHistory(
                    id = UUID.randomUUID().toString(),
                    city = city.text.toString(),
                    startDate = start.text.toString(),
                    endDate = end.text.toString(),
                    rating = rating.text.toString().toIntOrNull() ?: 3
                )
                histories.add(trip)
                selected = trip
                saveLastSelected(trip.id)
                renderList()
                updateStats()
                updateSelectedInfo()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun seedData() {
        histories.add(
            TravelHistory(
                id = UUID.randomUUID().toString(),
                city = "도쿄",
                startDate = "2024-03-01",
                endDate = "2024-03-05",
                rating = 5
            )
        )
        histories.add(
            TravelHistory(
                id = UUID.randomUUID().toString(),
                city = "오사카",
                startDate = "2024-04-10",
                endDate = "2024-04-12",
                rating = 4
            )
        )
    }

    private fun renderList(keyword: String = "") {
        binding.tvHistoryList.text =
            histories
                .filter { it.city.contains(keyword, true) }
                .joinToString("\n") {
                    val fav = if (it.isFavorite) "⭐" else ""
                    "$fav ${it.city} (${it.rating}점)"
                }
    }

    private fun updateStats() {
        val avg = histories.map { it.rating }.average()
        val best = histories.maxByOrNull { it.rating }?.city ?: "-"
        binding.tvStats.text =
            "총 여행 ${histories.size}회 · 평균 ${"%.1f".format(avg)}점 · 최고 $best"
    }

    private fun updateSelectedInfo() {
        selected?.let {
            val fav = if (it.isFavorite) "⭐ 즐겨찾기" else "일반"
            binding.tvSelectedInfo.text =
                "선택됨: ${it.city} (${it.startDate}~${it.endDate}) · ${it.rating}점 · $fav"
        } ?: run {
            binding.tvSelectedInfo.text = "선택된 여행 없음"
        }
    }

    private fun saveLastSelected(id: String) {
        prefs.edit().putString("last_selected_id", id).apply()
    }

    private fun restoreLastSelected() {
        val id = prefs.getString("last_selected_id", null) ?: return
        selected = histories.find { it.id == id }
    }
}

   /* private fun restoreLastViewed() {
        val lastId = prefs.getString(KEY_LAST_VIEWED_ID, null) ?: return
        val index = filteredList.indexOfFirst { it.id == lastId }
        if (index >= 0) selectedHistoryIndex = index
    }

    private fun saveLastViewed() {
        val history = filteredList.getOrNull(selectedHistoryIndex) ?: return
        prefs.edit().putString(KEY_LAST_VIEWED_ID, history.id).apply()
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
        val lastViewedId = prefs.getString(KEY_LAST_VIEWED_ID, null)

        filteredList = list.sortedWith(
            compareByDescending<TravelHistory> { it.id == favoriteId }
                .thenByDescending { it.id == lastViewedId }
        )

        if (filteredList.isEmpty()) {
            binding.tvHistoryList.text = "표시할 여행이 없어요"
            selectedHistoryIndex = -1
            return
        }

        binding.tvHistoryList.text = filteredList.mapIndexed { index, it ->
            val recent = if (it.id == lastViewedId) "🕒 " else ""
            val fav = if (it.id == favoriteId) "⭐ " else ""
            "${index + 1}. $fav$recent${it.city} (${it.startDate} ~ ${it.endDate}) · ${it.rating}/5"
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
}*/