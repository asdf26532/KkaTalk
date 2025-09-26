package com.han.reservation

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RequestActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RequestAdapter
    private val database = FirebaseDatabase.getInstance().reference

    // 테스트용 ID
    private val guideId: String = "GUIDE_001"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request)

        recyclerView = findViewById(R.id.recyclerViewRequests)
        adapter = RequestAdapter(
            onAccept = { reservationId -> updateReservationStatus(reservationId, "예약 확정") },
            onReject = { reservationId -> updateReservationStatus(reservationId, "예약 거절") }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadRequests()

    }

    private fun loadRequests() {
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
    }

    private fun updateReservationStatus(reservationId: String, status: String) {
        database.child("reservations").child(reservationId).child("status")
            .setValue(status)
            .addOnSuccessListener {
                Toast.makeText(this, "예약 상태 변경: $status", Toast.LENGTH_SHORT).show()
                loadRequests() // 새로고침
            }
            .addOnFailureListener {
                Toast.makeText(this, "변경 실패", Toast.LENGTH_SHORT).show()
            }
    }

}