package com.han.tripnote.ui.detail

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.han.tripnote.data.model.Trip
import com.han.tripnote.databinding.ActivityTripDetailBinding
import com.han.tripnote.ui.add.AddTripActivity

class TripDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTripDetailBinding
    private lateinit var trip: Trip

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTripDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        trip = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("trip", Trip::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("trip")!!
        }

        bindTripInfo()

        // 수정 버튼 클릭
        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, AddTripActivity::class.java)
            intent.putExtra("trip", trip) //수정 대상 전달
            startActivity(intent)
        }
    }

    private fun bindTripInfo() {
        binding.tvDetailTitle.text = trip.title
        binding.tvDetailLocation.text = trip.location
        binding.tvDetailDate.text = "${trip.startDate} ~ ${trip.endDate}"
    }
    }