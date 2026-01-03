package com.han.reservation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.han.reservation.databinding.ActivityLabBinding


class LabActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityLabBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastShakeTime = 0L
    private val SHAKE_THRESHOLD = 12f

    private var lastTapTime = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var longPressRunnable: Runnable? = null

    companion object {
        private const val LAB_DOUBLE_TAP_TOAST = "lab_double_tap_toast"
        private const val LAB_LONG_PRESS_HINT = "lab_long_press_hint"
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLabBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("lab_prefs", MODE_PRIVATE)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        binding.btnLabCelebrate.setOnClickListener {
            runReserveCelebration()
        }

        binding.btnLabCancelTone.setOnClickListener {
            runCancelToneDialog()
        }

        setupLabButtons()

        RoleManager.checkAdmin(currentUser.uid) { isAdmin ->
            if (!isAdmin) {
                Toast.makeText(
                    this,
                    "접근 권한이 없습니다",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
                return@checkAdmin
            }

            // ✅ 관리자만 여기 도달
            initView()
        }

        setupRecyclerView()

        val experiment = LabExperiments.find(LabKeys.QUICK_RESERVE)

        LabExperimentRunner.runSafely(this, experiment) {
            initQuickReserveExperiment()
        }


        binding.btnDashboard.setOnClickListener {
            startActivity(
                Intent(this, LabDashboardActivity::class.java)
            )
        }

        binding.switchDoubleTapToast.isChecked =
            prefs.getBoolean(LAB_DOUBLE_TAP_TOAST, false)

        binding.switchDoubleTapToast.setOnCheckedChangeListener { _, checked ->
            prefs.edit()
                .putBoolean(LAB_DOUBLE_TAP_TOAST, checked)
                .apply()
        }

        binding.root.setOnTouchListener { _, event ->
            if (!prefs.getBoolean(LAB_DOUBLE_TAP_TOAST, false)) return@setOnTouchListener false
            if (event.action != MotionEvent.ACTION_DOWN) return@setOnTouchListener false

            val now = System.currentTimeMillis()
            if (now - lastTapTime < 300) {
                Toast.makeText(this, "LAB: 더블 탭 감지됨", Toast.LENGTH_SHORT).show()
                lastTapTime = 0L
            } else {
                lastTapTime = now
            }
            false
        }

        binding.switchDoubleTapToast.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean(LAB_DOUBLE_TAP_TOAST, checked).apply()
        }

        binding.switchLongPressHint.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean(LAB_LONG_PRESS_HINT, checked).apply()
        }

        binding.root.setOnTouchListener { _, event ->
            if (prefs.getBoolean(LAB_LONG_PRESS_HINT, false)) {
                handleLongPress(event)
            }
            if (prefs.getBoolean(LAB_DOUBLE_TAP_TOAST, false)) {
                handleDoubleTap(event)
            }
            false
        }



    }

    private fun initQuickReserveExperiment() {
        throw IllegalStateException("실험 실패 시뮬레이션")
    }

    private fun setupRecyclerView() {
        adapter = LabAdapter(
            experiments = LabExperiments.experiments,
            prefs = prefs
        )
        binding.recyclerLab.adapter = adapter
    }

    private fun initView() {
        binding = ActivityLabBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecycler()
    }

    private fun setupLabButtons() {
        binding.btnLabCelebrate.setOnClickListener {
            runReserveCelebration()
        }

        binding.btnLabCancelTone.setOnClickListener {
            runCancelToneDialog()
        }
    }

    private fun runReserveCelebration() {
        if (!prefs.getBoolean("lab_reserve_celebration", false)) {
            Toast.makeText(this, "Lab에서 OFF 상태입니다", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "예약 완료 🎉", Toast.LENGTH_SHORT).show()

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    150,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(150)
        }
    }

    private fun runCancelToneDialog() {
        if (!prefs.getBoolean("lab_cancel_tone", false)) {
            Toast.makeText(this, "Lab에서 OFF 상태입니다", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("잠깐만요 😢")
            .setMessage(
                "정말 예약을 취소하실 건가요?\n" +
                        "조금 아쉬운데요…"
            )
            .setPositiveButton("그래도 취소") { _, _ ->
                Toast.makeText(this, "취소 처리 (샘플)", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("다시 생각할래요", null)
            .show()
        }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!prefs.getBoolean(LAB_SHAKE_TOAST, false)) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val acceleration = kotlin.math.sqrt(x * x + y * y + z * z)
        val currentTime = System.currentTimeMillis()

        if (acceleration > SHAKE_THRESHOLD &&
            currentTime - lastShakeTime > 1000
        ) {
            lastShakeTime = currentTime
            Toast.makeText(
                this,
                "📳 흔들림 감지! (Lab 이스터에그)",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun handleDoubleTap(event: MotionEvent) {
        if (event.action != MotionEvent.ACTION_DOWN) return

        val now = System.currentTimeMillis()
        if (now - lastTapTime < 300) {
            Toast.makeText(this, "LAB: 더블 탭 감지됨", Toast.LENGTH_SHORT).show()
            lastTapTime = 0L
        } else {
            lastTapTime = now
        }
    }

    private fun handleLongPress(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                longPressRunnable = Runnable {
                    Toast.makeText(
                        this,
                        "LAB 힌트: 여기는 실험실입니다",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                handler.postDelayed(longPressRunnable!!, 600)
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                longPressRunnable?.let { handler.removeCallbacks(it) }
            }
        }
    }

}

