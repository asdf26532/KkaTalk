package com.han.kkatalk2

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.han.kkatalk2.databinding.ActivityReservationBinding
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

class ReservationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReservationBinding
    private lateinit var db: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var guideId: String
    private var isGuideMode: Boolean = false

    // 가이드가 등록한 예약 가능 날짜
    private val availableDates = mutableSetOf<LocalDate>()
    private var datesRef: DatabaseReference? = null
    private var valueListener: ValueEventListener? = null

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReservationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "예약 가능 날짜"

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference

        // ChatActivity 에서 전달받음
        guideId = intent.getStringExtra("guideId") ?: ""
        isGuideMode = intent.getBooleanExtra("isGuide", false)

        if (guideId.isEmpty()) {
            toast("가이드 정보가 없습니다.")
            finish()
            return
        }

        // DB 참조: /guide/{guideId}/availableDates
        datesRef = db.child("guide").child(guideId).child("availableDates")

        // 캘린더 초기화
        initCalendar()

        // Firebase에서 날짜 가져오기
        attachDatesListener()
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
        valueListener?.let { datesRef?.removeEventListener(it) }
    }

    /** 캘린더 초기화 */
    private fun initCalendar() {
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(12)
        val endMonth = currentMonth.plusMonths(12)

        binding.calendarView.setup(startMonth, endMonth, java.time.DayOfWeek.SUNDAY)
        binding.calendarView.scrollToMonth(currentMonth)

        binding.calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: android.view.View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.bind(day)
            }
        }
    }

    /** 날짜를 누르면 동작 */
    inner class DayViewContainer(view: android.view.View) :
        ViewContainer(view) {
        val textView = com.google.android.material.textview.MaterialTextView(view.context)

        lateinit var day: CalendarDay

        init {
            (view as android.widget.FrameLayout).addView(textView)

            textView.setOnClickListener {
                if (day.owner == DayOwner.THIS_MONTH) {
                    val date = day.date
                    val dateStr = date.format(dateFormatter)

                    if (isGuideMode) {
                        toggleDate(date, dateStr)
                    } else {
                        if (availableDates.contains(date)) {
                            toast("예약 가능: $dateStr")
                        } else {
                            toast("예약 불가: $dateStr")
                        }
                    }
                }
            }
        }

        fun bind(day: CalendarDay) {
            this.day = day
            textView.text = day.date.dayOfMonth.toString()

            // 오늘 표시
            if (day.date == LocalDate.now()) {
                textView.setTextColor(android.graphics.Color.RED)
            } else {
                textView.setTextColor(android.graphics.Color.BLACK)
            }

            // 예약 가능 날짜 표시
            if (availableDates.contains(day.date)) {
                textView.setBackgroundColor(android.graphics.Color.parseColor("#90CAF9")) // 파란 배경
            } else {
                textView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        }
    }

    /** Firebase에서 예약 가능 날짜 가져오기 */
    private fun attachDatesListener() {
        valueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                availableDates.clear()
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    try {
                        availableDates.add(LocalDate.parse(key, dateFormatter))
                    } catch (_: Exception) {
                    }
                }
                binding.calendarView.notifyCalendarChanged()
                Log.d("Reservation", "Loaded dates: $availableDates")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Reservation", "Load failed: ${error.message}")
            }
        }
        datesRef?.addValueEventListener(valueListener as ValueEventListener)
    }

    /** 가이드: 날짜 토글 (추가/삭제) */
    private fun toggleDate(date: LocalDate, dateStr: String) {
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

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
