package com.han.kkatalk2

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.han.kkatalk2.databinding.ActivityReservationBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.util.Calendar

class ReservationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReservationBinding
    private lateinit var db: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var guideId: String
    private var isGuideMode: Boolean = false

    private val available = HashSet<CalendarDay>()
    private var decorator: AvailableDateDecorator? = null
    private var datesRef: DatabaseReference? = null
    private var valueListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReservationBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_reservation)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "예약 가능 날짜"

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference

        // ChatActivity에서 전달
        guideId = intent.getStringExtra("guideId") ?: ""
        isGuideMode = intent.getBooleanExtra("isGuide", false)

        if (guideId.isEmpty()) {
            Toast.makeText(this, "가이드 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // DB 참조: /guide/{guideId}/availableDates
        datesRef = db.child("guide").child(guideId).child("availableDates")

        // 현재 데이터 로드 & 실시간 반영
        attachDatesListener()

        // 탭/선택 동작
        binding.calendarView.setOnDateChangedListener { _, date, _ ->
            val dateStr = toDateString(date)
            if (isGuideMode) {
                toggleDate(date, dateStr) // 가이드: 추가/삭제
            } else {
                // 사용자: 읽기 전용
                if (available.contains(date)) {
                    toast("예약 가능: $dateStr")
                } else {
                    toast("예약 불가: $dateStr")
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish(); true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 리스너 해제
        valueListener?.let { datesRef?.removeEventListener(it) }
    }

    /** Firebase에서 availableDates를 읽어와 달력에 데코 적용 */
    private fun attachDatesListener() {
        valueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                available.clear()
                for (child in snapshot.children) {
                    val key = child.key ?: continue // yyyy-MM-dd
                    parseDate(key)?.let { available.add(it) }
                }
                applyDecorator()
                Log.d("Reservation", "Loaded dates: $available")
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Reservation", "Load failed: ${error.message}")
            }
        }
        datesRef?.addValueEventListener(valueListener as ValueEventListener)
    }

    /** 데코레이터 갱신 */
    private fun applyDecorator() {
        decorator?.let { binding.calendarView.removeDecorator(it) }
        decorator = AvailableDateDecorator(available.toSet())
        binding.calendarView.addDecorator(decorator!!)
    }

    /** 가이드: 날짜 토글 (추가/삭제) */
    private fun toggleDate(date: CalendarDay, dateStr: String) {
        val ref = datesRef ?: return
        ref.child(dateStr).get().addOnSuccessListener { s ->
            if (s.exists()) {
                ref.child(dateStr).removeValue()
                    .addOnSuccessListener { toast("해제: $dateStr") }
                    .addOnFailureListener { e -> toast("해제 실패: ${e.message}") }
            } else {
                ref.child(dateStr).setValue(true)
                    .addOnSuccessListener { toast("추가: $dateStr") }
                    .addOnFailureListener { e -> toast("추가 실패: ${e.message}") }
            }
        }
    }

    /** yyyy-MM-dd 문자열로 */
    private fun toDateString(day: CalendarDay): String =
        String.format("%04d-%02d-%02d", day.year, day.month + 1, day.day) // month는 0-base

    /** yyyy-MM-dd → CalendarDay (MaterialCalendarView는 month 0-base) */
    private fun parseDate(s: String): CalendarDay? {
        val p = s.split("-")
        if (p.size != 3) return null
        return try {
            val y = p[0].toInt()
            val m = p[1].toInt() - 1
            val d = p[2].toInt()
            CalendarDay.from(y, m, d)
        } catch (_: Exception) { null }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

}