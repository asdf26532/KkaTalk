package com.han.reservation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var repo: FirebaseRepository

    private lateinit var btnMenu: ImageButton
    private lateinit var tvTitle: TextView
    private lateinit var cardReservation: LinearLayout
    private lateinit var cardBooking: LinearLayout
    private lateinit var cardRequest: LinearLayout

    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        repo = FirebaseRepository()

        // 로그인된 사용자 확인
        val currentUser = auth.currentUser
        if (currentUser != null) {
            userId = currentUser.uid
            Log.d(TAG, "현재 로그인 유저: $userId")
        } else {
            Toast.makeText(this, "로그인 정보가 없습니다. 로그인 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 뷰 초기화
        btnMenu = findViewById(R.id.btnMenu)
        tvTitle = findViewById(R.id.tvTitle)
        cardReservation = findViewById(R.id.cardReservation)
        cardBooking = findViewById(R.id.cardBooking)
        cardRequest = findViewById(R.id.cardRequest)

        // 메뉴 버튼
        btnMenu.setOnClickListener {
            Toast.makeText(this, "메뉴 클릭됨!", Toast.LENGTH_SHORT).show()
        }

        // 예약 버튼
        cardReservation.setOnClickListener {
            val dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("예약 날짜를 선택하세요")
                    .build()

            dateRangePicker.show(supportFragmentManager, "date_range_picker")

            dateRangePicker.addOnPositiveButtonClickListener { selection ->
                val startDateMillis = selection.first
                val endDateMillis = selection.second

                if (startDateMillis != null && endDateMillis != null) {
                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val startDate = format.format(Date(startDateMillis))
                    val endDate = format.format(Date(endDateMillis))

                    Toast.makeText(this, "선택: $startDate ~ $endDate", Toast.LENGTH_SHORT).show()

                    // 실제 예약 로직
                    val intent = Intent(this, DetailActivity::class.java)
                    intent.putExtra("userId", userId)
                    intent.putExtra("startDate", startDate)
                    intent.putExtra("endDate", endDate)
                    startActivity(intent)
                }
            }
        }

        // 예약 관리 (유저)
        cardBooking.setOnClickListener {
            val intent = Intent(this, ListActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        // 예약 요청 관리 (가이드)
        cardRequest.setOnClickListener {
            val guideRef = database.child("guides").child(userId)
            guideRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // 이미 가이드 등록됨 → 가이드 예약 관리
                        val intent = Intent(this@MainActivity, RequestActivity::class.java)
                        startActivity(intent)
                    } else {
                        // 가이드 등록 안됨 → 등록 화면
                        val intent = Intent(this@MainActivity, GuideRegisterActivity::class.java)
                        startActivity(intent)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "오류: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        /*buttonLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "로그아웃되었습니다", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }*/

    }
}