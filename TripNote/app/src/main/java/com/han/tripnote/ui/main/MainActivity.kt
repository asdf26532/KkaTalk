package com.han.tripnote.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.han.tripnote.R
import com.han.tripnote.data.model.Trip
import com.han.tripnote.ui.add.AddTripActivity
import com.han.tripnote.util.TripStorage

class MainActivity : AppCompatActivity() {

    private val tripList = mutableListOf<Trip>()
    private lateinit var adapter: TripAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.rvTripList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 어댑터 연결
        adapter = TripAdapter(tripList)
        recyclerView.adapter = adapter

        // 저장된 여행 목록 불러오기
        val savedTrips = TripStorage.load(this)
        tripList.addAll(savedTrips)
        adapter.notifyDataSetChanged()

        // 여행 추가 버튼
        findViewById<View>(R.id.fabAddTrip).setOnClickListener {
            val intent = Intent(this, AddTripActivity::class.java)
            startActivityForResult(intent, 100)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {

            val trip = Trip(
                id = data?.getStringExtra("id") ?: "",
                title = data?.getStringExtra("title") ?: "",
                location = data?.getStringExtra("location") ?: "",
                startDate = data?.getStringExtra("startDate") ?: "",
                endDate = data?.getStringExtra("endDate") ?: ""
            )

            tripList.add(trip)
            adapter.notifyItemInserted(tripList.size - 1)

            // 여행 목록 저장
            TripStorage.save(this, tripList)
        }
    }

}