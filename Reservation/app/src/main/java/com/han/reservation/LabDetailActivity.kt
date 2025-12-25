package com.han.reservation

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.han.reservation.databinding.ActivityLabDetailBinding

class LabDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLabDetailBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var experiment: LabExperiment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("lab_prefs", MODE_PRIVATE)

        val key = intent.getStringExtra("experiment_key") ?: return
        experiment = LabExperiments.experiments.first { it.key == key }

        bindExperiment()
        initSwitch()
    }

    private fun bindExperiment() {
        binding.tvTitle.text = experiment.title
        binding.tvDescription.text = experiment.description

        binding.tvBadge.text = experiment.badge.name
        binding.tvBadge.visibility =
            if (experiment.badge == ExperimentBadge.NONE) View.GONE else View.VISIBLE
    }

    private fun initSwitch() {
        val enabled = prefs.getBoolean(experiment.key, false)
        binding.switchExperiment.isChecked = enabled

        binding.switchExperiment.setOnCheckedChangeListener { _, checked ->
            prefs.edit()
                .putBoolean(experiment.key, checked)
                .putBoolean("${experiment.key}_used", false)
                .apply()
        }
    }
}