package com.han.reservation

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.han.reservation.databinding.ActivityLabBinding


class LabActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLabBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("lab_prefs", MODE_PRIVATE)

       // applyQuickReserveExperiment()

        setupRecyclerView()

        val experiment = LabExperiments.find(LabKeys.QUICK_RESERVE)

        LabExperimentRunner.runSafely(this, experiment) {
            initQuickReserveExperiment()
        }
    }

    private fun initQuickReserveExperiment() {
        throw IllegalStateException("실험 실패 시뮬레이션")
    }

    private fun setupRecyclerView() {
        adapter = LabAdapter(
            experiments = LabExperiments.experiments,
            prefs = prefs
        )
        binding.recyclerLab.adapter = adapter
    }

}