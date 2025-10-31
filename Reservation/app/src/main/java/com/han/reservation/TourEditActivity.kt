package com.han.reservation

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_tour_edit.*
import java.util.*

class TourEditActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var tourId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour_edit)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("tours")

        tourId = intent.getStringExtra("tourId")

        if (tourId != null) {
            loadTourData()
        }

        btnSaveTour.setOnClickListener {
            saveTour()
        }

        btnDeleteTour.setOnClickListener {
            deleteTour()
        }
    }

    private fun loadTourData() {
        database.child(tourId!!).get().addOnSuccessListener { snapshot ->
            val tour = snapshot.getValue(Tour::class.java)
            if (tour != null) {
                editTourTitle.setText(tour.title)
                editTourDesc.setText(tour.description)
                editTourPrice.setText(tour.price.toString())
            }
        }
    }

    private fun saveTour() {
        val title = editTourTitle.text.toString().trim()
        val desc = editTourDesc.text.toString().trim()
        val price = editTourPrice.text.toString().trim().toIntOrNull() ?: 0
        val uid = auth.currentUser?.uid ?: return

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val id = tourId ?: UUID.randomUUID().toString()
        val tour = Tour(
            tourId = id,
            guideId = uid,
            title = title,
            description = desc,
            price = price
        )

        database.child(id).setValue(tour).addOnSuccessListener {
            Toast.makeText(this, "투어 저장 완료", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun deleteTour() {
        if (tourId == null) {
            Toast.makeText(this, "삭제할 투어가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        database.child(tourId!!).removeValue().addOnSuccessListener {
            Toast.makeText(this, "삭제 완료", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}