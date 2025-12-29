package com.han.reservation

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.han.reservation.databinding.ActivityLabBinding


class LabActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLabBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        prefs = getSharedPreferences("lab_prefs", MODE_PRIVATE)

        val currentUser = auth.currentUser
        if (currentUser == null) {
            finish()
            return
        }

        RoleManager.checkAdmin(currentUser.uid) { isAdmin ->
            if (!isAdmin) {
                Toast.makeText(
                    this,
                    "접근 권한이 없습니다",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
                return@checkAdmin
            }

            // ✅ 관리자만 여기 도달
            initView()
        }

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

    private fun initView() {
        binding = ActivityLabBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecycler()
    }

}