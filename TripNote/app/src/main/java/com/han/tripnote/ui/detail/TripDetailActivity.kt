package com.han.tripnote.ui.detail

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.han.tripnote.R

class TripDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_detail)

        val tvTitle = findViewById<TextView>(R.id.tvDetailTitle)
        val tvLocation = findViewById<TextView>(R.id.tvDetailLocation)
        val tvDate = findViewById<TextView>(R.id.tvDetailDate)

        val title = intent.getStringExtra("title")
        val location = intent.getStringExtra("location")
        val startDate = intent.getStringExtra("startDate")
        val endDate = intent.getStringExtra("endDate")

        tvTitle.text = title
        tvLocation.text = location
        tvDate.text = "$startDate ~ $endDate"
    }
}