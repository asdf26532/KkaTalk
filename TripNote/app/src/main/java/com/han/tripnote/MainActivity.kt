package com.han.tripnote

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.han.tripnote.databinding.ActivityMainBinding
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var historyStorage: TravelHistoryStorage

    private var selectedHistoryIndex: Int = -1
    private var filteredList: List<TravelHistory> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        historyStorage = TravelHistoryStorage(this)

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
    }

    private fun showHistoryList(list: List<TravelHistory>) {
        filteredList = list

        if (list.isEmpty()) {
            binding.tvHistoryList.text = "검색 결과가 없어요"
            selectedHistoryIndex = -1
            return
        }

        binding.tvHistoryList.text = list.mapIndexed { index, it ->
            "${index + 1}. ${it.city} (${it.startDate} ~ ${it.endDate}) · ${it.rating}/5"
        }.joinToString("\n\n")

        binding.tvHistoryList.setOnClickListener {
            selectedHistoryIndex = 0
        }
    }


    private fun filterHistoryByCity(keyword: String) {
        val all = historyStorage.loadAll()

        if (keyword.isBlank()) {
            showHistoryList(all)
            showBestTrip()
            return
        }

        val filtered = all.filter {
            it.city.contains(keyword, ignoreCase = true)
        }

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