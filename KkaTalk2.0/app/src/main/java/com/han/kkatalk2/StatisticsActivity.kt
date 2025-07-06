package com.han.kkatalk2

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.google.firebase.database.*

class StatisticsActivity : AppCompatActivity() {

    private lateinit var userCountText: TextView
    private lateinit var reportCountText: TextView
    private lateinit var userBarChart: BarChart

    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        supportActionBar?.title = "앱 통계"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        userCountText = findViewById(R.id.tvUserCount)
        reportCountText = findViewById(R.id.tvReportCount)
        userBarChart = findViewById(R.id.userBarChart)


        dbRef = FirebaseDatabase.getInstance().reference

        fetchUserCount()
        fetchReportCount()
        fetchTodayUserCountBar()
        //fetchUserGrowthTrend()
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

    /*private fun fetchUserGrowthTrend() {
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
                    dataSet.color = getColor(R.color.colorPrimary)
                    userChart.data = LineData(dataSet)
                    userChart.invalidate()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }*/

    // 신규 가입자 확인
    private fun fetchTodayUserCountBar() {
        val todayStart = getStartOfTodayMillis()
        val todayEnd = todayStart + 24 * 60 * 60 * 1000

        dbRef.child("user").orderByChild("timestamp")
            .startAt(todayStart.toDouble()).endAt(todayEnd.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val todayCount = snapshot.childrenCount.toInt()

                    // BarEntry(x축, y축)
                    val entries = listOf(BarEntry(1f, todayCount.toFloat()))

                    val barDataSet = BarDataSet(entries, "오늘 가입자 수")
                    barDataSet.color = getColor(R.color.colorPrimary)

                    val data = BarData(barDataSet)
                    data.barWidth = 0.5f

                    userBarChart.data = data
                    userBarChart.description.text = "오늘 가입자 통계"
                    userBarChart.invalidate()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("StatisticsActivity", "BarChart load error: ${error.message}")
                }
            })
    }

    // 오늘 0시 타임스탬프 구하는 함수
    private fun getStartOfTodayMillis(): Long {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = now
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}