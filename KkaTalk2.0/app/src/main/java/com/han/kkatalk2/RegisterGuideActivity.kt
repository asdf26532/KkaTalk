package com.han.kkatalk2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterGuideActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_guide)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("guide")

        val edtName = findViewById<EditText>(R.id.edt_name)
        val edtLocation = findViewById<EditText>(R.id.edt_location)
        val edtRate = findViewById<EditText>(R.id.edt_rate)
        val edtPhone = findViewById<EditText>(R.id.edt_phone)
        val edtContent = findViewById<EditText>(R.id.edt_content)
        val btnRegister = findViewById<Button>(R.id.btn_register)

        btnRegister.setOnClickListener {
            val name = edtName.text.toString()
            val location = edtLocation.text.toString()
            val rate = edtRate.text.toString()
            val phone = edtPhone.text.toString()
            val content = edtContent.text.toString()
            val userId = auth.currentUser?.uid ?: ""

            if (name.isNotEmpty() && location.isNotEmpty() && rate.isNotEmpty() && phone.isNotEmpty()) {
                val guide = Guide(name, userId, phone, location, rate, content, "")

                database.child(userId).setValue(guide).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "가이드 등록 완료!", Toast.LENGTH_SHORT).show()
                        finish() // 액티비티 종료
                    } else {
                        Toast.makeText(this, "등록 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "모든 필드를 입력하세요!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
