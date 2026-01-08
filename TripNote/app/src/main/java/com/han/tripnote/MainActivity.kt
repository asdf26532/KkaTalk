package com.han.tripnote

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.han.tripnote.databinding.ActivityMainBinding
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val todayPlaces = mutableListOf(
        TravelPlace("광안리"),
        TravelPlace("해운대"),
        TravelPlace("자갈치시장")
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

    }

    private fun addPlace() {
        val nextIndex = todayPlaces.size + 1
        todayPlaces.add(TravelPlace("새로운 장소 $nextIndex"))
        updateUI()
    }

    private fun removePlace() {
        if (todayPlaces.isNotEmpty()) {
            todayPlaces.removeAt(todayPlaces.lastIndex)
            updateUI()
        }
    }

    private fun updateUI() {
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

        binding.tvSummaryComment.text = generateComment(todayPlaces.size)
    }

    private fun generateComment(count: Int): String {
        return when {
            count == 1 -> "가볍게 한 곳만 다녀온 하루였어요"
            count <= 3 -> "여유로운 일정의 여행이었어요"
            else -> "알차게 많이 돌아다닌 하루였어요"
        }
    }
}