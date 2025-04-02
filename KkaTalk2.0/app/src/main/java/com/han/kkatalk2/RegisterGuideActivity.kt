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

    private lateinit var auth: FirebaseAuth
    private lateinit var guideDatabase: DatabaseReference
    private lateinit var userDatabase: DatabaseReference

    // intent에서 guideId를 받아오면 수정 모드, guideId가 없으면 새로운 가이드 등록
    private var guideId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_guide)

        auth = FirebaseAuth.getInstance()
        guideDatabase = FirebaseDatabase.getInstance().getReference("guide")
        userDatabase = FirebaseDatabase.getInstance().getReference("user")

        val edtName = findViewById<EditText>(R.id.edt_name)
        val edtLocation = findViewById<EditText>(R.id.edt_location)
        val edtRate = findViewById<EditText>(R.id.edt_rate)
        val edtPhone = findViewById<EditText>(R.id.edt_phone)
        val edtContent = findViewById<EditText>(R.id.edt_content)
        val btnRegister = findViewById<Button>(R.id.btn_register)

        val userId = auth.currentUser?.uid ?: ""

        // 수정 모드인지 확인
        guideId = intent.getStringExtra("guideId")

        if (guideId != null) {
            // 기존 데이터 불러오기 (수정 모드)
            loadGuideData(guideId!!, edtName, edtLocation, edtRate, edtPhone, edtContent, btnRegister)
        }

        userDatabase.child(userId).child("nick").get().addOnSuccessListener { snapshot ->
            val nick = snapshot.value as? String ?: ""

            btnRegister.setOnClickListener {
                val name = edtName.text.toString()
                val location = edtLocation.text.toString()
                val rate = edtRate.text.toString()
                val phone = edtPhone.text.toString()
                val content = edtContent.text.toString()


                if (name.isNotEmpty() && location.isNotEmpty() && rate.isNotEmpty() && phone.isNotEmpty()) {
                    if (guideId == null) {
                        // 새 가이드 등록
                        val newGuideId = guideDatabase.push().key ?: return@setOnClickListener
                        val guide = Guide(name, userId, nick, phone, location, rate, content, "")

                        guideDatabase.child(newGuideId).setValue(guide).addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(this, "가이드 등록 완료!", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this, "등록 실패", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        // 기존 가이드 수정
                        val updates = mapOf(
                            "name" to name,
                            "locate" to location,
                            "rate" to rate,
                            "phoneNumber" to phone,
                            "content" to content
                        )

                        guideDatabase.child(guideId!!).updateChildren(updates).addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(this, "가이드 수정 완료!", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this, "수정 실패", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "모든 필드를 입력하세요!", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "닉네임 정보를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

        private fun loadGuideData(
            guideId: String,
            edtName: EditText,
            edtLocation: EditText,
            edtRate: EditText,
            edtPhone: EditText,
            edtContent: EditText,
            btnRegister: Button
        ) {
            guideDatabase.child(guideId).get().addOnSuccessListener { snapshot ->
                val guide = snapshot.getValue(Guide::class.java)
                if (guide != null) {
                    edtName.setText(guide.name)
                    edtLocation.setText(guide.locate)
                    edtRate.setText(guide.rate)
                    edtPhone.setText(guide.phoneNumber)
                    edtContent.setText(guide.content)
                    btnRegister.text = "수정하기"
                }
            }.addOnFailureListener {
                Toast.makeText(this, "가이드 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
}

