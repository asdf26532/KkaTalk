package com.han.reservation

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.han.reservation.Tour
import com.han.reservation.Reservation
import java.text.SimpleDateFormat
import java.util.*

class TourDetailActivity : AppCompatActivity() {

    private lateinit var titleView: TextView
    private lateinit var locationView: TextView
    private lateinit var dateView: TextView
    private lateinit var priceView: TextView
    private lateinit var descView: TextView
    private lateinit var btnReserve: Button

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var tourId: String? = null
    private var selectedTour: Tour? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour_detail)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        // View 연결
        titleView = findViewById(R.id.textTourTitle)
        locationView = findViewById(R.id.textTourLocation)
        dateView = findViewById(R.id.textTourDate)
        priceView = findViewById(R.id.textTourPrice)
        descView = findViewById(R.id.textTourDescription)
        btnReserve = findViewById(R.id.btnReserveTour)

        tourId = intent.getStringExtra("tourId")

        if (tourId.isNullOrEmpty()) {
            Toast.makeText(this, "투어 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadTourDetail(tourId!!)

        btnReserve.setOnClickListener {
            reserveTour()
        }
    }




}