package com.han.kkatalk2

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.google.firebase.database.*

class StatisticsActivity : AppCompatActivity() {

    private lateinit var userCountText: TextView
    private lateinit var reportCountText: TextView
    private lateinit var userChart: LineChart

    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        supportActionBar?.title = "앱 통계"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        userCountText = findViewById(R.id.tvUserCount)
        reportCountText = findViewById(R.id.tvReportCount)
        userChart = findViewById(R.id.userChart)

        dbRef = FirebaseDatabase.getInstance().reference

        fetchUserCount()
        fetchReportCount()
        fetchUserGrowthTrend()
    }

    private fun fetchUserCount() {
        dbRef.child("user").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userCount = snapshot.childrenCount
                userCountText.text = userCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchReportCount() {
        dbRef.child("reports").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reportCount = snapshot.childrenCount
                reportCountText.text = reportCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // 사용자 가입 증가 추이 시뮬레이션
    private fun fetchUserGrowthTrend() {
        dbRef.child("user").orderByChild("timestamp")  // User에 timestamp 필요
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val entries = mutableListOf<Entry>()
                    var index = 0f
                    snapshot.children.forEach {
                        entries.add(Entry(index, ++index)) // 임의 데이터
                    }
                    val dataSet = LineDataSet(entries, "가입자 수 증가 추이")
                    dataSet.setDrawFilled(true)
                    dataSet.color = getColor(R.color.purple_700)
                    userChart.data = LineData(dataSet)
                    userChart.invalidate()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}