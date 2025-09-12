package com.han.reservation

import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class BookingActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var btnConfirm: Button

    private var selectedDate: String = ""

    private val repo = FirebaseRepository()

    // 현재 로그인한 유저(회원) / 가이드 더미 (임시)
    private val currentUserId = "USER_001" // 로그인한 유저 ID
    private val guideId = "GUIDE_001" // 더미 가이드 ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        calendarView = findViewById(R.id.calendarView)
        btnConfirm = findViewById(R.id.btnConfirmReservation)

        // 날짜 선택 이벤트
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            selectedDate = format.format(cal.time)
            Toast.makeText(this, "선택한 날짜: $selectedDate", Toast.LENGTH_SHORT).show()
        }

        // 예약 버튼 클릭
        btnConfirm.setOnClickListener {
            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "날짜를 먼저 선택하세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val reservation = Reservation(
                id = UUID.randomUUID().toString(),
                userId = currentUserId,
                guideId = guideId,
                date = selectedDate,
                status = "pending"
            )

            repo.createReservation(reservation,
                onComplete = {
                    Toast.makeText(this, "예약 완료!", Toast.LENGTH_SHORT).show()
                    finish()
                },
                onError = { e ->
                    Toast.makeText(this, "예약 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}