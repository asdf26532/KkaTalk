package com.han.reservation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.han.reservation.databinding.ActivityLabDashboardBinding

class LabDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLabDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupList()
    }

    private fun setupList() {
        val experiments = LabExperiments.experiments
        binding.recyclerView.adapter =
            LabDashboardAdapter(experiments)
    }
}