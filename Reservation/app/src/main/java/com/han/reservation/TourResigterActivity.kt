package com.han.reservation

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.han.reservation.databinding.ActivityTourRegisterBinding
import java.util.UUID

class TourRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTourRegisterBinding
    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTourRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonSaveTour.setOnClickListener {
            val title = binding.editTitle.text.toString().trim()
            val desc = binding.editDescription.text.toString().trim()
            val location = binding.editLocation.text.toString().trim()
            val price = binding.editPrice.text.toString().trim().toIntOrNull() ?: 0
            val imageUrl = binding.editImageUrl.text.toString().trim()

            val guideId = auth.currentUser?.uid ?: return@setOnClickListener

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tourId = UUID.randomUUID().toString()
            val newTour = Tour(tourId, guideId, title, desc, location, price, imageUrl)

            db.child("tours").child(tourId).setValue(newTour)
                .addOnSuccessListener {
                    Toast.makeText(this, "상품 등록 완료!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "등록 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
