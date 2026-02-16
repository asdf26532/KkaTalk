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
import androidx.lifecycle.ViewModelProvider
import com.han.tripnote.data.model.Trip
import com.han.tripnote.ui.viewmodel.TripViewModel
import java.util.Calendar
import android.widget.ArrayAdapter
import com.han.tripnote.data.model.TripStatus

class AddTripActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTripBinding
    private lateinit var viewModel: TripViewModel

    private var editTrip: Trip? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTripBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TripViewModel::class.java]

        val statusList = TripStatus.values().map { it.name }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            statusList
        )
        binding.spinnerStatus.adapter = adapter

        // Edit Mode
        editTrip = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("trip", Trip::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("trip")
        }

        if (editTrip != null) {
            binding.etTitle.setText(editTrip!!.title)
            binding.etLocation.setText(editTrip!!.location)
            binding.etStartDate.setText(editTrip!!.startDate)
            binding.etEndDate.setText(editTrip!!.endDate)
            binding.etMemo.setText(editTrip!!.memo ?: "")

            val index = TripStatus.values().indexOf(editTrip!!.status)
            binding.spinnerStatus.setSelection(index)

            binding.btnSave.text = "수정 완료"
        }

        // 저장 버튼
        binding.btnSave.setOnClickListener {
            saveTrip()
        }
    }

    private fun saveTrip() {
        val title = binding.etTitle.text.toString()
        val location = binding.etLocation.text.toString()
        val startDate = binding.etStartDate.text.toString()
        val endDate = binding.etEndDate.text.toString()
        val memo = binding.etMemo.text.toString()

        val selectedStatus =
            TripStatus.valueOf(binding.spinnerStatus.selectedItem.toString())

        // 간단 유효성 검사
        if (title.isEmpty() || location.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }


        //  추가 / 수정 분기 핵심
        val trip = if (editTrip != null) {
            // 수정 → 기존 id 유지
            editTrip!!.copy(
                title = title,
                location = location,
                startDate = startDate,
                endDate = endDate,
                memo = memo,
                status = selectedStatus
            )
        } else {
            // 추가 → 새 id 생성
            Trip(
                id = UUID.randomUUID().toString(),
                title = title,
                location = location,
                startDate = startDate,
                endDate = endDate,
                memo = memo,
                status = selectedStatus
            )
        }

        viewModel.upsertTrip(trip)
        finish()
    }
}