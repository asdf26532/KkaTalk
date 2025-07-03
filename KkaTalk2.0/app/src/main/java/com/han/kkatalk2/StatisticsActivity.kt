package com.han.kkatalk2

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.han.kkatalk2.databinding.ActivityStatisticsBinding
import java.text.SimpleDateFormat
import java.util.*

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var database: DatabaseReference

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "통계 정보"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        database = FirebaseDatabase.getInstance().reference

        loadStatistics()
    }

    private fun loadStatistics() {
        val today = dateFormat.format(Date())
        var totalUsers = 0
        var totalReports = 0
        var todayUsers = 0
        var todayGuides = 0
        var todayReports = 0

        // 사용자 수
        database.child("user").get().addOnSuccessListener { snapshot ->
            for (child in snapshot.children) {
                totalUsers++
                val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L
                if (isToday(timestamp)) todayUsers++
            }
            binding.txtTotalUsers.text = "총 사용자 수: $totalUsers"
            binding.txtTodayUsers.text = "오늘 가입자 수: $todayUsers"
        }

        // 신고 수
        database.child("reports").get().addOnSuccessListener { snapshot ->
            for (child in snapshot.children) {
                totalReports++
                val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L
                if (isToday(timestamp)) todayReports++
            }
            binding.txtTotalReports.text = "총 신고 수: $totalReports"
            binding.txtTodayReports.text = "오늘 신고 수: $todayReports"
        }

        // 가이드 수
        database.child("guide").get().addOnSuccessListener { snapshot ->
            for (child in snapshot.children) {
                val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L
                if (isToday(timestamp)) todayGuides++
            }
            binding.txtTodayGuides.text = "오늘 등록된 가이드 수: $todayGuides"
        }
    }

    private fun isToday(timestamp: Long): Boolean {
        val date = Date(timestamp)
        val nowDate = dateFormat.format(Date())
        val compareDate = dateFormat.format(date)
        return nowDate == compareDate
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
