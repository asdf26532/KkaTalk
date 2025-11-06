package com.han.reservation

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class TourRegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().reference

    private lateinit var etTitle: EditText
    private lateinit var etLocation: EditText
    private lateinit var etPrice: EditText
    private lateinit var etDate: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour_register)

        auth = FirebaseAuth.getInstance()

        etTitle = findViewById(R.id.etTitle)
        etLocation = findViewById(R.id.etLocation)
        etPrice = findViewById(R.id.etPrice)
        etDate = findViewById(R.id.etDate)
        etDescription = findViewById(R.id.etDescription)
        btnRegister = findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener {
            saveTourToFirebase()
        }
    }

    private fun saveTourToFirebase() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val title = etTitle.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val price = etPrice.text.toString().trim()
        val date = etDate.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (title.isEmpty() || location.isEmpty() || price.isEmpty() || date.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val tourId = database.child("tours").push().key ?: return
        val tour = Tour(
            id = tourId,
            guideId = currentUser.uid,
            title = title,
            location = location,
            price = price.toInt(),
            date = date,
            description = description
        )

        database.child("tours").child(tourId).setValue(tour)
            .addOnSuccessListener {
                Toast.makeText(this, "투어 등록이 완료되었습니다!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "등록 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}