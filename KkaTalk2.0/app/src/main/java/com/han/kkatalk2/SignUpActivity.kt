package com.han.kkatalk2

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

    // 이메일 중복 체크 여부
    private var isEmailChecked = false
    private var lastCheckedEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 인증 초기화
        mAuth = Firebase.auth

        // DB 초기화
        mDbRef = Firebase.database.reference

        // 이메일 중복 확인 버튼 클릭
        binding.btnEmailCheck.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "올바른 이메일 형식을 입력하세요.", Toast.LENGTH_SHORT).show()
                isEmailChecked = false
                return@setOnClickListener
            }

            checkEmailDuplicate(email) { isDuplicate ->
                if (isDuplicate) {
                    Toast.makeText(this, "이미 사용 중인 이메일입니다.", Toast.LENGTH_SHORT).show()
                    isEmailChecked = false
                } else {
                    Toast.makeText(this, "사용 가능한 이메일입니다.", Toast.LENGTH_SHORT).show()
                    isEmailChecked = true
                    lastCheckedEmail = email
                }
            }
        }

        // 회원가입 버튼 클릭
        binding.btnSignUp.setOnClickListener {
            val name = binding.edtName.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            val nick = binding.edtNick.text.toString().trim()

            // 입력값 검사
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || nick.isEmpty()) {
                Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 이메일 형식 검사
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "이메일 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 중복 확인을 안 했거나, 확인한 이메일이 현재 이메일과 다름
            if (!isEmailChecked || lastCheckedEmail != email) {
                Toast.makeText(this, "이메일 중복 확인을 해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 회원가입 실행
            signUp(name, email, password, nick)
        }
    }

    // 이메일 중복 확인
    private fun checkEmailDuplicate(email: String, onResult: (Boolean) -> Unit) {
        mAuth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val isDuplicate = task.result?.signInMethods?.isNotEmpty() == true
                    onResult(isDuplicate)
                } else {
                    onResult(true) // 오류 발생 시 중복으로 간주
                }
            }
    }

    // Firebase Auth를 이용한 회원가입
    private fun signUp(name: String, email: String, password: String, nick: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "회원가입 성공", Toast.LENGTH_LONG).show()
                    addUserToDatabase(name, email, mAuth.currentUser?.uid!!, nick)
                    startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "회원가입 실패: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun addUserToDatabase(name:String, email:String, uId: String, nick: String) {
        mDbRef.child("user").child(uId).setValue(User(name, email, uId, nick, defaultProfileImageUrl))
    }
}