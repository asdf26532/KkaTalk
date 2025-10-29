package com.han.reservation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ListActivity : AppCompatActivity() {

    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var adapter: ReservationAdapter
    private val allReservations = mutableListOf<Reservation>()

    private val database = FirebaseDatabase.getInstance().reference
    private lateinit var auth: FirebaseAuth
    private var userId: String = ""

    object ReservationStatus {
        const val PENDING = "pending"      // 예약 요청중
        const val CONFIRMED = "confirmed"  // 예약 확정
        const val COMPLETED = "completed"  // 예약 완료
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        auth = FirebaseAuth.getInstance()

        recyclerView = findViewById(R.id.recyclerViewReservations)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayoutReservations)

        // 어댑터 설정
        adapter = ReservationAdapter { reservationId ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("reservationId", reservationId)
            startActivity(intent)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 탭 초기화
        val tabs = listOf("예약 요청중", "예약 확정", "예약 완료")
        tabs.forEach { tabLayout.addTab(tabLayout.newTab().setText(it)) }

        // 탭 선택 시 필터링
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                filterReservations(tab.text.toString())
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        checkLoginAndLoad()
    }

    // 로그인 상태 확인 및 예약 불러오기
    private fun checkLoginAndLoad() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        userId = currentUser.uid
        Log.d("ListActivity", "현재 로그인된 유저 ID: $userId")
        loadReservations()
    }

    // 예약 목록 로드
    private fun loadReservations() {
        database.child("reservations")
            .orderByChild("userId")
            .equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    allReservations.clear()

                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val currentTime = System.currentTimeMillis()

                    for (child in snapshot.children) {
                        val reservation = child.getValue(Reservation::class.java) ?: continue
                        reservation.id = child.key ?: ""

                        try {
                            val endDateStr = reservation.date
                                ?.split("~")
                                ?.getOrNull(1)
                                ?.trim()
                                ?: reservation.date?.trim()
                                ?: ""

                            if (endDateStr.isNotEmpty()) {
                                val endDateMillis = dateFormat.parse(endDateStr)?.time ?: 0L

                                // 확정 상태 + 여행 종료일이 현재보다 과거면 완료 처리
                                if (reservation.status == ReservationStatus.CONFIRMED &&
                                    endDateMillis < currentTime
                                ) {
                                    reservation.status = ReservationStatus.COMPLETED
                                    database.child("reservations")
                                        .child(reservation.id)
                                        .child("status")
                                        .setValue(ReservationStatus.COMPLETED)
                                }
                            }
                        } catch (e: Exception) {
                            Log.w("ListActivity", "날짜 파싱 실패: ${e.message}")
                        }

                        allReservations.add(reservation)
                    }

                    val selectedTab = findViewById<TabLayout>(R.id.tabLayoutReservations)
                        .getTabAt(findViewById<TabLayout>(R.id.tabLayoutReservations).selectedTabPosition)
                        ?.text?.toString() ?: "예약 요청중"

                    filterReservations(selectedTab)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ListActivity", "예약 불러오기 실패: ${error.message}")
                }
            })
    }

    // 탭별 예약 필터링
    private fun filterReservations(filter: String) {
        val status = when (filter) {
            "예약 요청중" -> ReservationStatus.PENDING
            "예약 확정" -> ReservationStatus.CONFIRMED
            "예약 완료" -> ReservationStatus.COMPLETED
            else -> null
        }

        val filteredList = if (status == null) {
            allReservations
        } else {
            allReservations.filter { it.status == status }
        }

        adapter.submitList(filteredList)
    }

    // 상단 뒤로가기 처리
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
