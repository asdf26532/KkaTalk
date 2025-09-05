package com.han.reservation

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var btnLoadGuides: Button
    private lateinit var btnCreateReservation: Button

    private val repo = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 가이드 불러오기 버튼
        btnLoadGuides.setOnClickListener {
            repo.fetchGuides(
                onComplete = { guides ->
                    Toast.makeText(this, "가이드 ${guides.size}명 불러옴", Toast.LENGTH_SHORT).show()
                    for (g in guides) {
                        Log.d(TAG, "Guide: ${g.id}, ${g.name}, ${g.location}, ${g.price}")
                    }
                },
                onError = { e ->
                    Toast.makeText(this, "가이드 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "fetchGuides error: ${e.message}")
                }
            )
        }

        // 예약 생성 버튼
        btnCreateReservation.setOnClickListener {
            val reservation = Reservation(
                id = "",
                guideId = "testGuide01",
                userId = "testUser01",
                userName = "홍길동",
                contact = "010-1234-5678",
                date = "2025-09-10",
                time = "14:00",
                status = "reserved",
                createdAt = System.currentTimeMillis()
            )

            repo.createReservation(reservation) { success, id ->
                if (success) {
                    Toast.makeText(this, "예약 생성 성공! ID=$id", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Reservation created with id=$id")
                } else {
                    Toast.makeText(this, "예약 생성 실패: $id", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Reservation create failed: $id")
                }
            }
        }
    }
}