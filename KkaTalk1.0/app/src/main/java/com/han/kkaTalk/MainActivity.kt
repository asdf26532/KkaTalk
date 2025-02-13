package com.han.kkaTalk

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.han.kkaTalk.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(HomeFragment())

        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.chatting -> replaceFragment(ChattingFragment())
                R.id.setting -> replaceFragment(SettingFragment())

                else -> {
                }
            }
            true
        }
        // FCM 토큰 가져오기
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            val token = task.result
            Log.d("FCM", "FCM Token: $token")
            Toast.makeText(this, "FCM Token: $token", Toast.LENGTH_SHORT).show()

            // 토큰을 서버 (Firebase Realtime Database 또는 Firestore)에 저장할 수도 있음
            saveTokenToDatabase(token)
        })
    }

    private fun saveTokenToDatabase(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val databaseRef = FirebaseDatabase.getInstance().reference
            databaseRef.child("user").child(userId).child("fcmToken").setValue(token)
        }

    }

    private fun replaceFragment(fragment: Fragment) {

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.addToBackStack(null) // 백스택 추가
        fragmentTransaction.commit()
        Log.d("MainActivity", "Fragment replaced: ${fragment.javaClass.simpleName}") // 추가 로그
    }

    /*override fun onStart() {
        super.onStart()
        updateUserStatus("online")
    }

    override fun onStop() {
        super.onStop()
        // 마지막 접속 시간 업데이트
        updateUserStatus("offline")
    }

    private fun updateUserStatus(status: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val updates = mapOf(
            "status" to status,
            "lastActiveTime" to System.currentTimeMillis()
        )
        FirebaseDatabase.getInstance().getReference("user").child(userId).updateChildren(updates)
    }*/
}