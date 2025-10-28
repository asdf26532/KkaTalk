package com.han.reservation

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.han.reservation.databinding.ActivityTourDetailBinding
import java.util.UUID

class TourDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTourDetailBinding
    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTourDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tourId = intent.getStringExtra("tourId") ?: return

        loadTourDetail(tourId)

        binding.buttonReserve.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(this, "로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            reserveTour(tourId, currentUser.uid)
        }
    }

    private fun loadTourDetail(tourId: String) {
        db.child("tours").child(tourId).get().addOnSuccessListener {
            val tour = it.getValue(Tour::class.java)
            if (tour != null) {
                binding.textTitle.text = tour.title
                binding.textDescription.text = tour.description
                binding.textLocation.text = "지역: ${tour.location}"
                binding.textPrice.text = "₩${tour.price}"

                if (tour.imageUrl.isNotEmpty()) {
                    Glide.with(this).load(tour.imageUrl).into(binding.imageTour)
                }
            }
        }
    }

    private fun reserveTour(tourId: String, userId: String) {
        val reservationId = UUID.randomUUID().toString()
        val reservation = mapOf(
            "id" to reservationId,
            "tourId" to tourId,
            "userId" to userId,
            "timestamp" to System.currentTimeMillis()
        )

        db.child("reservations").child(reservationId).setValue(reservation)
            .addOnSuccessListener {
                Toast.makeText(this, "예약이 완료되었습니다!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "예약 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}