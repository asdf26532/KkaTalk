package com.han.reservation

import android.content.Intent
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
    private lateinit var cardReservation: LinearLayout
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
        cardReservation = findViewById(R.id.cardReservation)
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
                id = "g1",
                name = "김가이드",
                location = "서울",
                price = 50000,
                description = "일본 투어 전문 가이드"
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

        // 예약
        cardReservation.setOnClickListener{
            val intent = Intent(this, BookingActivity::class.java)
            startActivity(intent)
        }

        // 예약 관리
        cardBooking.setOnClickListener {

            val intent = Intent(this, ListActivity::class.java)
            startActivity(intent)
        }


    }
}