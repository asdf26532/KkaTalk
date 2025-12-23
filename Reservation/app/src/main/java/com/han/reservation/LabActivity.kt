package com.han.reservation

import android.content.SharedPreferences
import android.os.Bundle
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

        applyQuickReserveExperiment()
    }

    /*private fun initQuickReserveExperiment() {
        // 저장된 실험 플래그 불러오기
        val isEnabled = prefs.getBoolean("lab_quick_reserve", false)

        binding.switchQuickReserve.isChecked = isEnabled

        binding.switchQuickReserve.setOnCheckedChangeListener { _, checked ->
            prefs.edit()
                .putBoolean("lab_quick_reserve", checked)
                .apply()
        }
    }*/

    private fun applyQuickReserveExperiment() {
        val isEnabled = prefs.getBoolean("lab_quick_reserve", false)
        val isUsed = prefs.getBoolean("lab_quick_reserve_used", false)

        // 실험 OFF → 아무 영향 없음
        if (!isEnabled) return

        // 이미 한 번 적용됨
        if (isUsed) return

        // 사용자가 이미 입력한 경우
        if (binding.etMessage.text.isNotEmpty()) {
            markExperimentUsed()
            return
        }

        // 실험 효과 적용
        binding.etMessage.setText("안녕하세요! 예약 문의드립니다 🙂")
        binding.tvLabHint.visibility = View.VISIBLE

        markExperimentUsed()
    }

    private fun markExperimentUsed() {
        prefs.edit()
            .putBoolean("lab_quick_reserve_used", true)
            .apply()
    }


}