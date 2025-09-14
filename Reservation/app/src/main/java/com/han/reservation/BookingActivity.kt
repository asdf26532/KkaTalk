package com.han.reservation

import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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

        btnConfirm = findViewById(R.id.btnConfirmReservation)

        // 예약 버튼
        btnConfirm.setOnClickListener {
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

                    // 예약 객체 생성
                    val reservation = Reservation(
                        id = UUID.randomUUID().toString(),
                        userId = currentUserId,
                        guideId = guideId,
                        date = "$startDate ~ $endDate",
                        status = "pending"
                    )

                    // DB 저장
                    repo.createReservation(reservation) { success, idOrError ->
                        if (success) {
                            Toast.makeText(this, "예약 완료!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, "예약 실패: $idOrError", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}