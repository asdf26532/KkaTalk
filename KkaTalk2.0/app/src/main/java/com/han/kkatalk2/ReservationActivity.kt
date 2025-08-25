package com.han.kkatalk2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import com.han.kkatalk2.databinding.ActivityReservationBinding

class ReservationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReservationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val selectedDate = "$year-${month + 1}-$dayOfMonth"
            val guideUid = "exampleGuideUid" // 실제로는 auth.currentUser.uid or Intent로 전달된 UID

            val dbRef = FirebaseDatabase.getInstance().getReference("reservations").child(guideUid)

            if (isGuideMode) {
                // 👉 가이드 모드: 날짜 추가/삭제 토글
                dbRef.child(selectedDate).get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        // 이미 등록된 경우 → 삭제
                        dbRef.child(selectedDate).removeValue()
                        Toast.makeText(this, "$selectedDate 예약 가능 해제됨", Toast.LENGTH_SHORT).show()
                    } else {
                        // 등록되지 않은 경우 → 추가
                        dbRef.child(selectedDate).setValue(true)
                        Toast.makeText(this, "$selectedDate 예약 가능 추가됨", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // 👉 사용자 모드: 예약 가능 여부 확인
                dbRef.child(selectedDate).get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        Toast.makeText(this, "$selectedDate 예약 가능!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "$selectedDate 예약 불가", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }




    }
}