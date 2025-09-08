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
            Toast.makeText(this, "가이드 보기 클릭됨!", Toast.LENGTH_SHORT).show()
        }

        // 예약 관리 클릭
        cardBooking.setOnClickListener {
            Toast.makeText(this, "예약 관리 클릭됨!", Toast.LENGTH_SHORT).show()
        }

        // Firebase Repository 테스트
        cardFirebase.setOnClickListener {
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
    }
}