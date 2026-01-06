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

        binding.tvSummaryTitle.text = "부산 1일차 여행"
        binding.tvSummaryDesc.text = "광안리 → 해운대 → 자갈치시장"
        binding.tvSummaryTime.text = "총 3곳 방문"
    }
}