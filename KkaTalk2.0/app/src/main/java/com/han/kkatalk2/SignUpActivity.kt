package com.han.kkatalk2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.han.kkatalk2.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignUpBinding
    lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    // 기본 프로필 이미지 URL
    private val defaultProfileImageUrl = "${BuildConfig.STORAGE_BUCKET}/profile_default.png"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 인증 초기화
        mAuth = Firebase.auth

        // DB 초기화
        mDbRef = Firebase.database.reference

        binding.btnSignUp.setOnClickListener {
            val name = binding.edtName.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            val nick = binding.edtNick.text.toString().trim()

            // 닉네임 가공 (한글 포함 여부 확인 후 랜덤 생성)
            //val nickToSave = if (containsKorean(originalNick)) generateRandomNick() else originalNick

            signUp(name, email, password, nick)

        }
    }

  /*  // 한글 포함 여부 체크
    private fun containsKorean(text: String): Boolean {
        return text.any { it in '\uAC00'..'\uD7A3' }
    }

    // 랜덤 닉네임 생성
    private fun generateRandomNick(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        val random = (1..6).map { chars.random() }.joinToString("")
        return "user_$random"
    }*/

    // 회원 가입 기능
    private fun signUp(name:String, email:String, password:String, nick:String) {

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "회원가입 성공", Toast.LENGTH_LONG).show()
                    val intent: Intent = Intent(this@SignUpActivity, MainActivity::class.java)
                    startActivity(intent)
                    addUserToDatabase(name, email, mAuth.currentUser?.uid!!, nick)
                } else {
                    Toast.makeText(this, "회원가입 실패: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }

    }

    private fun addUserToDatabase(name:String, email:String, uId: String, nick: String) {
        mDbRef.child("user").child(uId).setValue(User(name, email, uId, nick, defaultProfileImageUrl))
    }
}