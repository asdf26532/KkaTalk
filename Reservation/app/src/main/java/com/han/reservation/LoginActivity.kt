package com.han.reservation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.han.reservation.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    lateinit var mAuth: FirebaseAuth
    lateinit var googleSignInClient: GoogleSignInClient

    private val RC_SIGN_IN = 9001 // Request code for Google Sign-In

    private val defaultStatusMessage = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase 인증 초기화
        mAuth = Firebase.auth


        // 회원가입 버튼 이벤트
        binding.btnSignUp.setOnClickListener{
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(intent)
        }
        // 카카오 로그인 버튼 클릭
        binding.btnKakao.setOnClickListener {
            kakaoSignIn()
        }
    }

    // 자체 로그인
    private fun login(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 성공 시 실행
                    val uid = task.result.user?.uid ?: return@addOnCompleteListener

                    checkBanAndProceed(uid) {
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    // 실패 시 실행
                    Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
                    Log.d("Login", "Error: ${task.exception}" )
                }
            }
    }

    // 구글 로그인
    private fun googleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                Log.w("LoginActivity", "Google sign in failed", e)
                Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // DB에 저장
    private fun addUserToDatabase(name: String, email: String, uId: String, nick: String) {

        val storage = FirebaseStorage.getInstance(BuildConfig.STORAGE_BUCKET)
        val storageRef = storage.getReference("profile_default.png")

        storageRef.downloadUrl
            .addOnSuccessListener { uri ->
                val profileImageUrl = uri.toString()
                Log.d("DEBUG", "다운로드 URL: $profileImageUrl")

                val user = User(name, email, uId, nick, profileImageUrl, defaultStatusMessage,"user")
                Firebase.database.reference.child("user").child(uId).setValue(user)
            }
            .addOnFailureListener { exception ->
                Log.e("LoginActivity", "프로필 이미지 URL 가져오기 실패: ${exception.message}")
                val user = User(name, email, uId, nick, "", defaultStatusMessage,"user")
                Firebase.database.reference.child("user").child(uId).setValue(user)
            }
    }


}