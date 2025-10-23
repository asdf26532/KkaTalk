package com.han.reservation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
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
import java.text.SimpleDateFormat
import java.util.Locale

class ListActivity : AppCompatActivity() {


    private lateinit var recyclerView: RecyclerView
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

        val tabLayout = findViewById<TabLayout>(R.id.tabLayoutReservations)

        recyclerView = findViewById(R.id.recyclerViewReservations)
        adapter = ReservationAdapter { reservationId ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("reservationId", reservationId)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 로그인 확인
        val currentUser = auth.currentUser
        if (currentUser != null) {
            userId = currentUser.uid
            Log.d("ListActivity", "현재 로그인된 유저 ID: $userId")
            loadReservations()
        } else {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }


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

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadReservations()
    }

    private fun filterReservations(filter: String) {
        val targetStatus = when (filter) {
            "예약 요청중" -> ReservationStatus.PENDING
            "예약 확정" -> ReservationStatus.CONFIRMED
            "예약 완료" -> ReservationStatus.COMPLETED
            else -> null
        }

        val filtered = if (targetStatus == null) {
            allReservations.toList()
        } else {
            allReservations.filter { it.status == targetStatus }
        }

        adapter.submitList(filtered)
    }

    private fun loadReservations() {
        database.child("reservations")
            .orderByChild("userId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val reservations = mutableListOf<Reservation>()
                    val today = System.currentTimeMillis()
                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                    for (child in snapshot.children) {
                        val reservation = child.getValue(Reservation::class.java)
                        reservation?.let {
                            it.id = child.key ?: ""

                            // 문자열 date → Long 변환
                            try {
                                val dateStr = it.date
                                val endDateStr = if (dateStr.contains("~")) {
                                    dateStr.split("~")[1].trim() // 끝 날짜
                                } else {
                                    dateStr.trim()
                                }

                                val endDateMillis = format.parse(endDateStr)?.time ?: 0L

                                // 예약완료 처리: 확정 + 날짜 지난 경우
                                if (it.status == ReservationStatus.CONFIRMED && endDateMillis < today) {
                                    it.status = ReservationStatus.COMPLETED
                                    database.child("reservations").child(it.id).child("status")
                                        .setValue(ReservationStatus.COMPLETED)
                                }
                            } catch (e: Exception) {
                                Log.e("ListActivity", "날짜 파싱 실패: ${e.message}")
                            }

                            reservations.add(it)
                        }
                    }

                    allReservations.clear()
                    allReservations.addAll(reservations)

                    // 현재 선택된 탭 확인
                    val tabLayout = findViewById<TabLayout>(R.id.tabLayoutReservations)
                    val selected = tabLayout.selectedTabPosition

                    if (selected < 0) {
                        // 앱 첫 진입 시 → 예약 요청중만 보여주기
                        filterReservations("예약 요청중")
                    } else {
                        // 그 외엔 현재 탭 기준으로 필터링
                        filterReservations(tabLayout.getTabAt(selected)?.text.toString())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ListActivity", "예약 불러오기 실패: ${error.message}")
                }
            })
    }

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