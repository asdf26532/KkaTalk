package com.han.tripnote.ui.main

import android.app.Activity
import android.app.AlertDialog
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
import android.widget.LinearLayout

class MainActivity : AppCompatActivity() {

    private val tripList = mutableListOf<Trip>()
    private lateinit var adapter: TripAdapter

    private lateinit var emptyLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.rvTripList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = TripAdapter(tripList) { position ->
            showDeleteDialog(position)
        }
        recyclerView.adapter = adapter

        emptyLayout = findViewById(R.id.layoutEmpty)

        // 저장된 여행 목록 불러오기
        val savedTrips = TripStorage.load(this)
        tripList.addAll(savedTrips)
        adapter.notifyDataSetChanged()

        updateEmptyView()

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

            updateEmptyView()

            // 여행 목록 저장
            TripStorage.save(this, tripList)
        }
    }

    private fun showDeleteDialog(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("여행 삭제")
            .setMessage("이 여행을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                tripList.removeAt(position)
                adapter.notifyItemRemoved(position)

                updateEmptyView()

                TripStorage.save(this, tripList)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun updateEmptyView() {
        if (tripList.isEmpty()) {
            emptyLayout.visibility = View.VISIBLE
        } else {
            emptyLayout.visibility = View.GONE
        }
    }
}