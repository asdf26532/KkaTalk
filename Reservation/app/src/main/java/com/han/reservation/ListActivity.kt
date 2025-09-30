package com.han.reservation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ListActivity : AppCompatActivity() {


    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReservationAdapter
    private val allReservations = mutableListOf<Reservation>()

    private val database = FirebaseDatabase.getInstance().reference

    // 테스트용 유저
    private val userId: String = "USER_001"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayoutReservations)

        recyclerView = findViewById(R.id.recyclerViewReservations)
        adapter = ReservationAdapter { reservationId ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("reservationId", reservationId)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        /*val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            userId = currentUser.uid
            loadReservations()
        } else {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
        }*/


        // 탭 추가
        val tabs = listOf("예약 요청중", "예약 확정", "예약 완료")
        tabs.forEach { tabLayout.addTab(tabLayout.newTab().setText(it)) }

        // 탭 선택 시 필터링
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val filter = tab.text.toString()
                filterReservations(filter)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        loadReservations()
    }

    private fun loadReservations() {
        database.child("reservations")
            .orderByChild("userId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val reservations = mutableListOf<Reservation>()
                    for (child in snapshot.children) {
                        val reservation = child.getValue(Reservation::class.java)
                        reservation?.let {
                            it.id = child.key ?: ""
                            reservations.add(it)
                        }
                    }
                    // allReservations 업데이트
                    allReservations.clear()
                    allReservations.addAll(reservations)

                    adapter.submitList(reservations)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ListActivity", "예약 불러오기 실패: ${error.message}")
                }
            })
    }

    private fun filterReservations(filter: String) {
        if (filter == "전체") {
            adapter.submitList(allReservations.toList())
        } else {
            val filtered = allReservations.filter { it.status == filter }
            adapter.submitList(filtered)
        }
    }
}