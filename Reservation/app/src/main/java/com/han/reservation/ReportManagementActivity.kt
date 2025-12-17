package com.han.reservation

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class ReportManagementActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReportAdapter
    private val reportList = mutableListOf<Report>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_management)

        recyclerView = findViewById(R.id.recyclerReports)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ReportAdapter(reportList)
        recyclerView.adapter = adapter

        fetchReports()

        // 툴바에 뒤로가기 버튼 추가
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

    }

    // 신고 내역 불러오기
    fun fetchReports() {
        val reportsRef = FirebaseDatabase.getInstance().getReference("reports")
        reportsRef.get().addOnSuccessListener { snapshot ->
            reportList.clear()
            for (child in snapshot.children) {
                val report = child.getValue(Report::class.java)
                Log.d("ReportCheck", "reportId=${report?.reportId}, isHandled=${report?.isHandled}")

                if (report != null && !report.isHandled) {
                    reportList.add(report)
                }
            }
            adapter.notifyDataSetChanged()
        }.addOnFailureListener {
            showCustomToast("신고 불러오기 실패: ${it.message}")
        }
    }

    // 뒤로 가기 버튼
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { // 뒤로가기 버튼 클릭 이벤트 처리
                Log.d("ProfileActivity", "뒤로가기 버튼 클릭됨")
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
