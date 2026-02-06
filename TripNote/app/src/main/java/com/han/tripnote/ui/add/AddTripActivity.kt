package com.han.tripnote.ui.add

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.han.tripnote.databinding.ActivityAddTripBinding
import java.util.UUID
import android.app.DatePickerDialog
import android.os.Build
import android.widget.Toast
import com.han.tripnote.data.model.Trip
import java.util.Calendar

class AddTripActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTripBinding

    private var isEditMode = false
    private var editTrip: Trip? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddTripBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Edit Mode
        editTrip = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("trip", Trip::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("trip")
        }

        // Edit Mode 판별
        if (editTrip != null) {
            isEditMode = true
            setupEditMode()
        }

        // 날짜 선택 처리
        binding.etStartDate.setOnClickListener {
            showDatePicker { date ->
                binding.etStartDate.setText(date)
            }
        }

        // 저장 버튼
        binding.btnSave.setOnClickListener {

            val title = binding.etTitle.text.toString()
            val location = binding.etLocation.text.toString()
            val startDate = binding.etStartDate.text.toString()
            val endDate = binding.etEndDate.text.toString()

            // 입력값 검증
            if (title.isBlank() || location.isBlank()
                || startDate.isBlank() || endDate.isBlank()
            ) {
                Toast.makeText(this, "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent().apply {
                putExtra("id", UUID.randomUUID().toString())
                putExtra("title", title)
                putExtra("location", location)
                putExtra("startDate", startDate)
                putExtra("endDate", endDate)
            }

            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun setupEditMode() {
        binding.etTitle.setText(editTrip?.title)
        binding.etLocation.setText(editTrip?.location)
        binding.etStartDate.setText(editTrip?.startDate)
        binding.etEndDate.setText(editTrip?.endDate)

        binding.btnSave.text = "수정 완료"
        supportActionBar?.title = "여행 수정"
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val date = "${year}.${month + 1}.${dayOfMonth}"
                onDateSelected(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}