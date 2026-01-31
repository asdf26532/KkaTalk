package com.han.tripnote.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.han.tripnote.R
import com.han.tripnote.data.model.Trip

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.rvTripList)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // [2일차] 더미 여행 데이터
        val dummyTrips = listOf(
            Trip("1", "도쿄 여행", "일본 도쿄", "2026.03.01", "2026.03.05"),
            Trip("2", "부산 여행", "대한민국 부산", "2026.04.10", "2026.04.12"),
            Trip("3", "파리 여행", "프랑스 파리", "2026.05.01", "2026.05.08")
        )

        recyclerView.adapter = TripAdapter(dummyTrips)
    }
}