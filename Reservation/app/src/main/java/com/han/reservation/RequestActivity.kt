package com.han.reservation

import android.os.Bundle
import android.util.Log
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
        const val STATUS_PENDING = "예약 요청중"
        const val STATUS_CONFIRMED = "예약 확정"
        const val STATUS_COMPLETED = "완료된 예약" // DB에 맞춰 사용
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request)

        recyclerView = findViewById(R.id.recyclerView)
        adapter = RequestAdapter(
            onAccept = { reservationId -> updateReservationStatus(reservationId, "예약 확정") },
            onReject = { reservationId -> updateReservationStatus(reservationId, "예약 거절") }
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

        listenRequestsRealtime()

        //loadRequests()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 리스너 detach
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
                // 현재 선택된 탭 기준으로 화면에 보일 목록 갱신
                val selected = tabLayout.selectedTabPosition.takeIf { it >= 0 } ?: 0
                applyFilterForTab(selected)
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

        // 어댑터에 리스트 넘김 (ListAdapter면 submitList 사용)
        // RequestAdapter가 ListAdapter라면 아래처럼 submitList 권장:
        adapter.submitList(displayRequests.toList())

        // 만약 커스텀 update 함수 사용 시:
        // adapter.updateData(displayRequests)
    }

    /*private fun loadRequests() {
        database.child("reservations")
            .orderByChild("guideId")
            .equalTo(guideId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val requests = mutableListOf<Reservation>()
                    for (child in snapshot.children) {
                        val reservation = child.getValue(Reservation::class.java)
                        reservation?.let {
                            it.id = child.key ?: ""
                            if (it.status == "예약 요청중") requests.add(it)
                        }
                    }
                    adapter.submitList(requests)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("GuideRequestsActivity", "가이드 예약 요청 불러오기 실패: ${error.message}")
                }
            })
    }*/

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

}