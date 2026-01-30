package com.han.tripnote

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.snackbar.Snackbar
import com.han.tripnote.databinding.ActivityMainBinding
import java.util.UUID
import android.text.InputType
import androidx.recyclerview.widget.LinearLayoutManager
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences

    private val histories = mutableListOf<TravelHistory>()
    private var selected: TravelHistory? = null
    private lateinit var memoAdapter: MemoAdapter
    private lateinit var historyAdapter: TravelHistoryAdapter

    private var lastDeleted: TravelHistory? = null
    private var lastDeletedIndex: Int = -1

    private var sortByRating = false
    private var sortByUpcoming = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("trip_prefs", MODE_PRIVATE)
        memoAdapter = MemoAdapter(emptyList())

        seedData()
        restoreLastSelected()

        if (selected == null) {
            selectMostRecentTrip()
        }

        historyAdapter = TravelHistoryAdapter(
            onClick = {
                selected = it
                saveLastSelected(it.id)
                updateSelectedInfo()
                historyAdapter.refreshSelection()
            },
            onLongClick = {
                selected = it
                showSummaryDialog(it)
            },
            isSelected = { it.id == selected?.id }
        )

        renderList()
        updateStats()
        updateSelectedInfo()

        binding.etSearch.addTextChangedListener {
            renderList(it.toString())
        }

        binding.btnAddTrip.setOnClickListener {
            showTripDialog()
        }

        binding.btnEditTrip.setOnClickListener {
            if (selected == null) {
                Toast.makeText(this, "수정할 여행을 선택하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showTripDialog(selected)
        }

        binding.btnDeleteTrip.setOnClickListener {
            deleteSelectedTrip()
        }

        binding.tvHistoryList.setOnClickListener {
            selected = histories.lastOrNull()
            selected?.let { saveLastSelected(it.id) }
            updateSelectedInfo()
        }

        binding.btnFavorite.setOnClickListener {
            selected?.let {
                it.isFavorite = !it.isFavorite
                renderList(binding.etSearch.text.toString())
                updateSelectedInfo()
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
            val text = binding.etMemo.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            selected?.let {
                it.memos.add(
                    TravelMemo(
                        id = UUID.randomUUID().toString(),
                        content = text
                    )
                )
                binding.etMemo.setText("")
                renderMemos(it)
                renderList(binding.etSearch.text.toString())
            }
        }

        binding.rvMemos.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = memoAdapter
        }

        binding.tvStats.setOnClickListener {
            when {
                !sortByRating && !sortByUpcoming -> sortByUpcoming = true
                sortByUpcoming -> {
                    sortByUpcoming = false
                    sortByRating = true
                }
                sortByRating -> sortByRating = false
            }
            renderList(binding.etSearch.text.toString())
            updateStats()
        }

        binding.tvSelectedInfo.setOnClickListener {
            selected?.let { showSummaryDialog(it) }
        }
    }

    private fun buildTripSummary(t: TravelHistory): String {
        val days = calculateDays(t.startDate, t.endDate)
        return """
            ✈️ 여행 요약
            도시: ${t.city}
            기간: ${t.startDate} ~ ${t.endDate} (${days}일)
            평점: ${t.rating}점
            메모 수: ${t.memos.size}개
        """.trimIndent()
    }

    private fun showSummaryDialog(t: TravelHistory) {
        AlertDialog.Builder(this)
            .setTitle("여행 요약")
            .setMessage(buildTripSummary(t))
            .setPositiveButton("확인", null)
            .show()
    }

    private fun showTripDialog(target: TravelHistory? = null) {
        val city = EditText(this).apply {
            hint = "도시 (필수)"
            setText(target?.city ?: "")
        }
        val start = EditText(this).apply {
            hint = "시작일 (YYYY-MM-DD)"
            setText(target?.startDate ?: "")
        }
        val end = EditText(this).apply {
            hint = "종료일 (YYYY-MM-DD)"
            setText(target?.endDate ?: "")
        }
        val rating = EditText(this).apply {
            hint = "평점 (1~5)"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(target?.rating?.toString() ?: "")
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 0)
            addView(city)
            addView(start)
            addView(end)
            addView(rating)
        }

        AlertDialog.Builder(this)
            .setTitle(if (target == null) "여행 추가" else "여행 수정")
            .setView(layout)
            .setPositiveButton("저장") { _, _ ->
                if (city.text.isBlank()) {
                    Toast.makeText(this, "도시는 필수입니다", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (target == null) {
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
                } else {
                    target.city = city.text.toString()
                    target.startDate = start.text.toString()
                    target.endDate = end.text.toString()
                    target.rating = rating.text.toString().toIntOrNull() ?: target.rating
                }

                renderList()
                updateStats()
                updateSelectedInfo()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun deleteSelectedTrip() {
        val target = selected ?: return

        lastDeletedIndex = histories.indexOf(target)
        lastDeleted = target
        histories.remove(target)
        selected = null
        saveLastSelected("")

        selectMostRecentTrip()

        renderList(binding.etSearch.text.toString())
        updateStats()
        updateSelectedInfo()

        Snackbar.make(binding.root, "여행이 삭제되었습니다", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                lastDeleted?.let {
                    histories.add(lastDeletedIndex, it)
                    selected = it
                    saveLastSelected(it.id)
                    renderList(binding.etSearch.text.toString())
                    updateStats()
                    updateSelectedInfo()
                }
            }
            .show()
    }

    private fun calculateDays(start: String, end: String): Long {
        return try {
            val s = LocalDate.parse(start)
            val e = LocalDate.parse(end)
            ChronoUnit.DAYS.between(s, e) + 1
        } catch (e: Exception) {
            0
        }
    }

    private fun calculateDDay(start: String): Long {
        return try {
            val today = LocalDate.now()
            val s = LocalDate.parse(start)
            ChronoUnit.DAYS.between(today, s)
        } catch (e: Exception) {
            Long.MAX_VALUE
        }
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
        val filtered = histories.filter {
            it.city.contains(keyword, true)
        }

        val list = when {
            sortByUpcoming -> filtered.sortedBy { calculateDDay(it.startDate) }
            sortByRating -> filtered.sortedByDescending { it.rating }
            else -> filtered
        }

        historyAdapter.submit(list)

        binding.tvRecentTrip.text =
            list.firstOrNull()?.let {
                "🔥 최근 여행 추천: ${it.city}"
            } ?: "🔥 추천 여행 없음"
    }

    private fun updateStats() {
        val avg = histories.map { it.rating }.average()
        val best = histories.maxByOrNull { it.rating }?.city ?: "-"
        val upcoming = histories
            .filter { calculateDDay(it.startDate) >= 0 }
            .minByOrNull { calculateDDay(it.startDate) }
            ?.city ?: "-"

        binding.tvStats.text =
            "총 ${histories.size}회 · 평균 ${"%.1f".format(avg)}점 · 최고 $best · 다음 $upcoming"
    }

    private fun updateSelectedInfo() {
        selected?.let {
            binding.tvSelectedInfo.text =
                "선택됨: ${it.city} (${it.startDate}~${it.endDate})"
            renderMemos(it)
        } ?: run {
            binding.tvSelectedInfo.text = "선택된 여행 없음"
            memoAdapter.submit(emptyList())
        }
    }

    private fun renderMemos(history: TravelHistory) {
        memoAdapter.submit(history.memos)
    }

    private fun saveLastSelected(id: String) {
        prefs.edit().putString("last_selected_id", id).apply()
    }

    private fun restoreLastSelected() {
        val id = prefs.getString("last_selected_id", null) ?: return
        selected = histories.find { it.id == id }
    }

    private fun selectMostRecentTrip() {
        selected = histories.maxByOrNull {
            calculateDDay(it.startDate)
        }
        selected?.let { saveLastSelected(it.id) }
    }
}
