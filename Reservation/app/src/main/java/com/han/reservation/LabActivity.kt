package com.han.reservation

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.han.reservation.databinding.ActivityLabBinding



private lateinit var binding: ActivityLabBinding
private lateinit var prefs: SharedPreferences

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityLabBinding.inflate(layoutInflater)
    setContentView(binding.root)

    prefs = getSharedPreferences("lab_prefs", MODE_PRIVATE)

    initQuickReserveExperiment()
}

private fun initQuickReserveExperiment() {
    // 저장된 실험 플래그 불러오기
    val isEnabled = prefs.getBoolean("lab_quick_reserve", false)

    binding.switchQuickReserve.isChecked = isEnabled

    binding.switchQuickReserve.setOnCheckedChangeListener { _, checked ->
        prefs.edit()
            .putBoolean("lab_quick_reserve", checked)
            .apply()
    }
}