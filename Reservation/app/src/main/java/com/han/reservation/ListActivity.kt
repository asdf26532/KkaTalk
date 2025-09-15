package com.han.reservation

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReservationAdapter
    private val repo = FirebaseRepository()

    // 로그인된 유저 ID (임시)
    private val currentUserId = "USER_001"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        recyclerView = findViewById(R.id.recyclerViewReservations)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ReservationAdapter(emptyList())
        recyclerView.adapter = adapter

        // 내 예약 목록 가져오기
        repo.fetchReservations { list ->
            val myList = list.filter { it.userId == currentUserId }
            if (myList.isEmpty()) {
                Toast.makeText(this, "예약이 없습니다.", Toast.LENGTH_SHORT).show()
            } else {
                adapter.updateData(myList)
                Log.d("ReservationList", "내 예약 ${myList.size}건 불러옴")
            }
        }

    }
}