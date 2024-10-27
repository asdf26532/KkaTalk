package com.han.kkaTalk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
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
import com.han.kkaTalk.databinding.ActivityLoginBinding
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import kotlin.jvm.functions.Function2

class LoginActivity : AppCompatActivity() {

    var mInterstitialAd : InterstitialAd? = null

    lateinit var binding: ActivityLoginBinding
    lateinit var mAuth: FirebaseAuth
    lateinit var googleSignInClient: GoogleSignInClient

    private val RC_SIGN_IN = 9001 // Request code for Google Sign-In

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        // 전면 광고 로드
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
                Log.d("AdLoad", "전면 광고 로드 성공")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("AdLoad", "전면 광고 로드 실패: ${adError.message}")
                mInterstitialAd = null
            }
        })



    }

    private fun showInterstitialAdAndProceed() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // 광고가 닫힌 후에 메인 화면으로 이동
                    navigateToMain()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    Log.d("AdShow", "전면 광고 표시 실패")
                    // 광고가 표시되지 않으면 바로 메인 화면으로 이동
                    navigateToMain()
                }
            }
            mInterstitialAd?.show(this)
        } else {
            // 광고가 로드되지 않았으면 바로 메인 화면으로 이동
            navigateToMain()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }




    private fun login(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 성공 시 실행
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_LONG).show()
                    showInterstitialAdAndProceed()
                } else {
                    // 실패 시 실행
                    Toast.makeText(this, "로그인 실패", Toast.LENGTH_LONG).show()
                    Log.d("Login", "Error: ${task.exception}" )
                }
            }
    }

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

                    // 파이어베이스 Realtime DB에 사용자 정보 저장
                    if (uid != null && name != null && email != null) {
                        addUserToDatabase(name, email, uid, nick?: "")
                    }

                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Google 로그인 성공", Toast.LENGTH_LONG).show()

                    // 전면 광고를 보여준 후 메인 화면으로 이동
                    showInterstitialAdAndProceed()
                } else {
                    // 로그인 실패 시
                    Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Google 로그인 실패", Toast.LENGTH_LONG).show()
                }
            }
    }

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
                addUserToDatabase(nickname ?: "", email, uid, nickname ?: "")

            } else {
                Log.e("KakaoLogin", "사용자 정보 업데이트 실패: ${error?.message}")

            }
        }
    }



    private fun addUserToDatabase(name: String, email: String, uId: String, nick: String) {
        val user = User(name, email, uId, nick)
        Firebase.database.reference.child("user").child(uId).setValue(user)
    }

}
