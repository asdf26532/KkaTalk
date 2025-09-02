package com.han.kkatalk2

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.han.kkatalk2.databinding.ActivityReservationBinding
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.view.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class ReservationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReservationBinding
    private lateinit var db: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var guideId: String
    private var isGuideMode: Boolean = false

    private val availableDates = mutableSetOf<LocalDate>()
    private var datesRef: DatabaseReference? = null
    private var valueListener: ValueEventListener? = null

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReservationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "예약 가능 날짜"

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference

        guideId = intent.getStringExtra("guideId") ?: ""
        isGuideMode = intent.getBooleanExtra("isGuide", false)

        binding.tvRoleHint.text = if (isGuideMode) {
            "가이드 모드: 날짜를 눌러 예약가능일을 추가/삭제하세요."
        } else {
            "사용자 모드: 파란색으로 표시된 날만 예약 가능합니다."
        }

        if (guideId.isEmpty()) {
            toast("가이드 정보가 없습니다.")
            finish()
            return
        }

        datesRef = db.child("guide").child(guideId).child("availableDates")

        initCalendar()
        attachDatesListener()
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        valueListener?.let { datesRef?.removeEventListener(it) }
    }


    private fun initCalendar() {
        val calendarView: CalendarView = binding.calendarView

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(12)
        val endMonth = currentMonth.plusMonths(12)

        calendarView.setup(startMonth, endMonth, DayOfWeek.SUNDAY)
        calendarView.scrollToMonth(currentMonth)

        // 월 헤더 (yyyy년 M월)
        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthHeaderContainer> {
            override fun create(view: View): MonthHeaderContainer = MonthHeaderContainer(view)
            override fun bind(container: MonthHeaderContainer, month: com.kizitonwose.calendar.core.CalendarMonth) {
                container.title.text = month.yearMonth.atDay(1).format(monthTitleFormatter)
            }
        }

        // 날짜 셀
        calendarView.dayBinder = object : DayBinder<DayContainer> {
            override fun create(view: View): DayContainer = DayContainer(view)
            override fun bind(container: DayContainer, day: CalendarDay) = container.bind(day)
        }
    }

    private inner class MonthHeaderContainer(view: View) : ViewContainer(view) {
        val title: TextView = view.findViewById(R.id.tvMonthTitle)
    }

    private inner class DayContainer(view: View) : ViewContainer(view) {
        private val root: FrameLayout = view.findViewById(R.id.dayRoot)
        private val tv: TextView = view.findViewById(R.id.tvDay)

        lateinit var day: CalendarDay

        init {
            tv.setOnClickListener {
                if (day.owner != DayOwner.THIS_MONTH) return@setOnClickListener

                val date = day.date
                val dateStr = date.format(dateFormatter)

                if (isGuideMode) {
                    // 가이드: 토글 저장
                    toggleDate(date, dateStr)
                } else {
                    // 사용자: 읽기 전용
                    if (availableDates.contains(date)) {
                        toast("예약 가능: $dateStr")
                    } else {
                        toast("예약 불가: $dateStr")
                    }
                }
            }
        }

        fun bind(day: CalendarDay) {
            this.day = day
            tv.text = day.date.dayOfMonth.toString()

            if (day.owner != DayOwner.THIS_MONTH) {
                tv.alpha = 0.3f
            } else {
                tv.alpha = 1f
            }

            // 오늘: 빨간 글씨
            tv.setTextColor(if (day.date == LocalDate.now()) Color.RED else Color.BLACK)

            // 예약 가능일: 파란 배경
            if (availableDates.contains(day.date)) {
                root.setBackgroundColor(Color.parseColor("#90CAF9"))
            } else {
                root.setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }

    // --- Firebase IO ---

    /** 날짜 목록 실시간 반영 */
    private fun attachDatesListener() {
        val ref = datesRef ?: return
        valueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                availableDates.clear()
                for (child in snapshot.children) {
                    val key = child.key ?: continue // yyyy-MM-dd
                    try {
                        availableDates.add(LocalDate.parse(key, dateFormatter))
                    } catch (_: Exception) { /* 무시 */ }
                }
                // 전체 갱신
                binding.calendarView.notifyCalendarChanged()
                Log.d("Reservation", "Loaded dates: $availableDates")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Reservation", "Load failed: ${error.message}")
            }
        }
        ref.addValueEventListener(valueListener as ValueEventListener)
    }

    /** 가이드: 날짜 토글 (추가/삭제) */
    private fun toggleDate(date: LocalDate, dateStr: String) {
        val ref = datesRef ?: return
        ref.child(dateStr).get().addOnSuccessListener { s ->
            if (s.exists()) {
                ref.child(dateStr).removeValue()
                    .addOnSuccessListener {
                        availableDates.remove(date)
                        binding.calendarView.notifyDateChanged(date)
                        toast("해제: $dateStr")
                    }
                    .addOnFailureListener { e -> toast("해제 실패: ${e.message}") }
            } else {
                ref.child(dateStr).setValue(true)
                    .addOnSuccessListener {
                        availableDates.add(date)
                        binding.calendarView.notifyDateChanged(date)
                        toast("추가: $dateStr")
                    }
                    .addOnFailureListener { e -> toast("추가 실패: ${e.message}") }
            }
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()



}