package com.han.kkatalk2

import android.os.Bundle
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
    }

    private fun fetchReports() {
        val reportsRef = FirebaseDatabase.getInstance().getReference("reports")
        reportsRef.get().addOnSuccessListener { snapshot ->
            reportList.clear()
            for (child in snapshot.children) {
                val report = child.getValue(Report::class.java)
                if (report != null) {
                    reportList.add(report)
                }
            }
            adapter.notifyDataSetChanged()
        }.addOnFailureListener {
            showCustomToast("신고 불러오기 실패")
        }
    }
}
