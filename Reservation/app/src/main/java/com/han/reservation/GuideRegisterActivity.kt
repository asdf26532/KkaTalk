package com.han.reservation

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class GuideRegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide_register)

        val editTextName = findViewById<EditText>(R.id.editTextGuideName)
        val editTextLocation = findViewById<EditText>(R.id.editTextGuideLocation)
        val editTextIntro = findViewById<EditText>(R.id.editTextGuideIntro)
        val buttonSave = findViewById<Button>(R.id.buttonSaveGuide)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        buttonSave.setOnClickListener {
            val name = editTextName.text.toString().trim()
            val location = editTextLocation.text.toString().trim()
            val intro = editTextIntro.text.toString().trim()

            if (name.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "이름과 지역은 필수입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val guideData = mapOf(
                "name" to name,
                "location" to location,
                "intro" to intro
            )

            FirebaseDatabase.getInstance().getReference("guides")
                .child(userId)
                .setValue(guideData)
                .addOnSuccessListener {
                    Toast.makeText(this, "가이드 등록 완료!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "등록 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}