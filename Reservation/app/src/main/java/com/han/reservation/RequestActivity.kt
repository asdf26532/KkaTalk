package com.han.reservation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class RequestActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RequestAdapter
    private val database = FirebaseDatabase.getInstance().reference

    // 테스트용 가이드 ID (로그인 연동 시 교체)
    private val guideId: String = "GUIDE_001"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request)

        recyclerView = findViewById(R.id.recyclerViewGuideRequests)
        adapter = GuideRequestAdapter(
            onAccept = { reservationId -> updateReservationStatus(reservationId, "예약 확정") },
            onReject = { reservationId -> updateReservationStatus(reservationId, "예약 거절") }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadRequests()

    }


}