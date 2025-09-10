package com.han.reservation

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var btnMenu: ImageButton
    private lateinit var tvTitle: TextView
    private lateinit var tvWelcome: TextView
    private lateinit var cardGuide: LinearLayout
    private lateinit var cardBooking: LinearLayout
    private lateinit var cardFirebase: LinearLayout

    private val repo = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 뷰 초기화
        btnMenu = findViewById(R.id.btnMenu)
        tvTitle = findViewById(R.id.tvTitle)
        tvWelcome = findViewById(R.id.tvWelcome)
        cardGuide = findViewById(R.id.cardGuide)
        cardBooking = findViewById(R.id.cardBooking)
        cardFirebase = findViewById(R.id.cardFirebase)

        // 메뉴 버튼 (예: 토스트만 출력)
        btnMenu.setOnClickListener {
            Toast.makeText(this, "메뉴 클릭됨!", Toast.LENGTH_SHORT).show()
        }

        // 가이드 보기 클릭
        cardGuide.setOnClickListener {
            // 더미 가이드 하나 생성
            val guide = Guide(
                name = "김가이드",
                location = "서울",
                price = 50000
            )

            repo.createGuide(guide) { success, id ->
                if (success) {
                    Toast.makeText(this, "가이드 생성 성공! ID=$id", Toast.LENGTH_SHORT).show()

                    // 생성 후 전체 가이드 조회
                    repo.fetchGuides { list ->
                        Toast.makeText(this, "가이드 ${list.size}명 있음", Toast.LENGTH_SHORT).show()
                        for (g in list) {
                            Log.d(TAG, "가이드: ${g.id}, ${g.name}, ${g.location}, ${g.price}")
                        }
                    }

                } else {
                    Toast.makeText(this, "가이드 생성 실패: $id", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 예약 관리
        cardBooking.setOnClickListener {
            // 더미 예약 만들기
            val reservation = Reservation(
                guideId = "testGuide01",
                date = "2025-09-10",
                time = "14:00"
            )

            repo.createReservation(reservation) { success, id ->
                if (success) {
                    Toast.makeText(this, "예약 생성 성공! ID=$id", Toast.LENGTH_SHORT).show()

                    // 생성 후 전체 예약 조회
                    repo.fetchReservations { list ->
                        Toast.makeText(this, "예약 ${list.size}건 있음", Toast.LENGTH_SHORT).show()
                        for (r in list) {
                            Log.d(TAG, "예약: ${r.id}, ${r.userName}, ${r.date} ${r.time}")
                        }
                    }

                } else {
                    Toast.makeText(this, "예약 생성 실패: $id", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }
}