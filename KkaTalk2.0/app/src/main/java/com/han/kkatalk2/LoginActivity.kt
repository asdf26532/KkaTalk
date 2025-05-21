package com.han.kkatalk2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import com.han.kkatalk2.databinding.ActivityLoginBinding
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient

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

        // SharedPreferences 초기화
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Firebase 인증 초기화
        mAuth = Firebase.auth

        // Google Sign-In 설정
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(com.firebase.ui.auth.R.string.default_web_client_id)) // Firebase 콘솔에서 발급된 Web client ID 입력
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 로그인 버튼 이벤트
        binding.btnLogin.setOnClickListener {

            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력하세요.", Toast.LENGTH_LONG).show()
            } else {
                login(email, password)
            }
        }

        // 회원가입 버튼 이벤트
        binding.btnSignUp.setOnClickListener{
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(intent)
        }

        // Google 로그인 버튼 이벤트
        binding.btnGoogle.setOnClickListener {
            googleSignIn()
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
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_LONG).show()
                    //showInterstitialAdAndProceed()
                } else {
                    // 실패 시 실행
                    Toast.makeText(this, "로그인 실패", Toast.LENGTH_LONG).show()
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
                Toast.makeText(this, "Google 로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 로그인 성공 시
                    val firebaseUser = mAuth.currentUser
                    val uid = firebaseUser?.uid
                    val name = firebaseUser?.displayName
                    val email = firebaseUser?.email
                    val nick = firebaseUser?.displayName //이름으로 저장 후 나중에 변경페이지 만들기

                    // SharedPreferences에 UID 저장
                    if (uid != null) {
                        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        sharedPref.edit().putString("userId", uid).apply()
                    }

                    // 파이어베이스 Realtime DB에 사용자 정보 저장
                    if (uid != null && name != null && email != null) {
                        addUserToDatabase(name, email, uid, nick?: "")
                    }

                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Google 로그인 성공", Toast.LENGTH_LONG).show()

                    // 전면 광고를 보여준 후 메인 화면으로 이동
                    //showInterstitialAdAndProceed()
                } else {
                    // 로그인 실패 시
                    Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Google 로그인 실패", Toast.LENGTH_LONG).show()
                }
            }
    }

    // 카카오 로그인
    private fun kakaoSignIn() {
        Log.d("KakaoLogin", "카카오 로그인 시작")
        // 카카오톡 설치 여부에 따라 로그인 방식 결정
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e("KakaoLogin", "카카오 로그인 실패: ${error.message}")
                Toast.makeText(this, "카카오 로그인 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            } else if (token != null) {
                Log.d("KakaoLogin", "카카오 로그인 성공, 토큰 획득")
                // 로그인 성공 시 사용자 정보 업데이트
                updateKakaoLoginUi()
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                Toast.makeText(this, "카카오 로그인 성공", Toast.LENGTH_LONG).show()
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    private fun updateKakaoLoginUi() {
        Log.d("KakaoLogin", "사용자 정보 업데이트 시작")
        UserApiClient.instance.me { user, error ->
            if (user != null) {
                Log.d("KakaoLogin", "사용자 정보 획득 성공: ${user.kakaoAccount?.profile?.nickname}")
                // 사용자 정보 업데이트
                val nickname = user.kakaoAccount?.profile?.nickname

                // Firebase DB에 사용자 정보 저장
                val uid = user.id.toString()
                val email = user.kakaoAccount?.email ?: ""

                // SharedPreferences에 UID 저장
                val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                sharedPref.edit().putString("userId", uid).apply()

                addUserToDatabase(nickname ?: "", email, uid, nickname ?: "")

            } else {
                Log.e("KakaoLogin", "사용자 정보 업데이트 실패: ${error?.message}")

            }
        }
    }



    private fun addUserToDatabase(name: String, email: String, uId: String, nick: String) {

        val storageRef = FirebaseStorage.getInstance(BuildConfig.STORAGE_BUCKET)
            .getReference("profile_default.png")

        storageRef.downloadUrl.addOnSuccessListener { uri ->
            val defaultProfileImageUrl = uri.toString()
            val user = User(name, email, uId, nick, defaultProfileImageUrl, defaultStatusMessage)
            Firebase.database.reference.child("user").child(uId).setValue(user)
        }.addOnFailureListener {
            // 실패 시 기본 URL 없이 저장하거나 오류 처리
            Log.e("LoginActivity", "프로필 이미지 URL 가져오기 실패: ${it.message}")
            val user = User(name, email, uId, nick, "", defaultStatusMessage)
            Firebase.database.reference.child("user").child(uId).setValue(user)
        }

    }

}