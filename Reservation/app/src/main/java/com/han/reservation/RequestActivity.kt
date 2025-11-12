package com.han.reservation

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RequestActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RequestAdapter
    private val database = FirebaseDatabase.getInstance().reference

    // 테스트용 ID
    private val guideId: String = "GUIDE_001"

    private val allRequests = mutableListOf<Reservation>()
    private val displayRequests = mutableListOf<Reservation>()

    private var dbListener: ValueEventListener? = null

    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_CONFIRMED = "confirmed"
        const val STATUS_COMPLETED = "completed"
        const val STATUS_REJECTED = "rejected"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request)

        recyclerView = findViewById(R.id.recyclerView)
        adapter = RequestAdapter(
            onAccept = { reservationId -> updateReservationStatus(reservationId, STATUS_CONFIRMED) },
            onReject = { reservationId -> updateReservationStatus(reservationId, STATUS_REJECTED) }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        tabLayout = findViewById(R.id.tabLayout)

        // 탭 생성
        tabLayout.addTab(tabLayout.newTab().setText("예약 요청중"))
        tabLayout.addTab(tabLayout.newTab().setText("예약 확정"))
        tabLayout.addTab(tabLayout.newTab().setText("완료된 예약"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                applyFilterForTab(tab.position)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        listenRequestsRealtime()

    }

    override fun onDestroy() {
        super.onDestroy()
        dbListener?.let {
            database.child("reservations").removeEventListener(it)
        }
    }

    private fun listenRequestsRealtime() {
        val ref = database.child("reservations")
        dbListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allRequests.clear()
                for (child in snapshot.children) {
                    val reservation = child.getValue(Reservation::class.java)
                    reservation?.let {
                        it.id = child.key ?: ""
                        // guideId로 먼저 필터링: DB에 맞게 guideId 저장되어 있어야 함
                        if (it.guideId == guideId) {
                            allRequests.add(it)
                        }
                    }
                }
                val selected = tabLayout.selectedTabPosition
                if (selected < 0) {
                    // 앱 첫 진입 시 -> "예약 요청중"만 보여줌
                    applyFilterForTab(0)
                } else {
                    // 그 외엔 현재 탭 기준
                    applyFilterForTab(selected)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RequestActivity", "DB error: ${error.message}")
                Toast.makeText(this@RequestActivity, "요청 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        }
        ref.addValueEventListener(dbListener as ValueEventListener)
    }

    private fun applyFilterForTab(tabPosition: Int) {
        displayRequests.clear()
        when (tabPosition) {
            0 -> displayRequests.addAll(allRequests.filter { it.status == STATUS_PENDING })
            1 -> displayRequests.addAll(allRequests.filter { it.status == STATUS_CONFIRMED })
            2 -> displayRequests.addAll(allRequests.filter { it.status == STATUS_COMPLETED })
            else -> displayRequests.addAll(allRequests)
        }
        adapter.submitList(displayRequests.toList())
    }


    private fun updateReservationStatus(reservationId: String, status: String) {
        database.child("reservations").child(reservationId).child("status")
            .setValue(status)
            .addOnSuccessListener {
                Toast.makeText(this, "예약 상태 변경: $status", Toast.LENGTH_SHORT).show()
                //loadRequests() // 새로고침
            }
            .addOnFailureListener {
                Toast.makeText(this, "변경 실패", Toast.LENGTH_SHORT).show()
            }
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