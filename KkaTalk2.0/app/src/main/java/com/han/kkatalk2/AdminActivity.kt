package com.han.kkatalk2

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        supportActionBar?.title = "관리자 대시보드"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 신고 관리
        findViewById<Button>(R.id.btnReportManagement).setOnClickListener {
            startActivity(Intent(this, ReportManagementActivity::class.java))
        }

        // 유저 관리
        findViewById<Button>(R.id.btnUserManagement).setOnClickListener {
            startActivity(Intent(this, UserManagementActivity::class.java))
        }

        // 통계 정보
        findViewById<Button>(R.id.btnStatistics).setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }


    }

    // 뒤로가기 눌렀을 때 종료
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}