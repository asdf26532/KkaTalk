package com.han.kkaTalk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.han.kkaTalk.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var userId: String
    private lateinit var userNick: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 툴바에 뒤로가기 버튼 추가
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        // Intent에서 사용자 UID와 닉네임 가져오기
        userId = intent.getStringExtra("uId") ?: ""
        userNick = intent.getStringExtra("nick") ?: ""

        // 닉네임 표시
        binding.tvNick.text = "닉네임: $userNick"

        // 사용자 프로필 이미지 로드
        loadProfileImage()

        // 상태 메시지 표시
        loadStatusMessage()

        // 대화하기 버튼 클릭 이벤트
        binding.btnChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("uId", userId)
            intent.putExtra("nick", userNick)
            intent.putExtra("profileImageUrl", getUserProfileImageUrl())
            startActivity(intent)
        }

        // 취소 버튼 클릭 이벤트
        binding.btnLogout.setOnClickListener {
            finish()
        }
    }

    // 뒤로 가기 버튼
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { // 뒤로가기 버튼 클릭 이벤트 처리
                Log.d("ProfileActivity", "뒤로가기 버튼 클릭됨")
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getUserProfileImageUrl(): String {
        var profileImageUrl = ""
        val userRef = FirebaseDatabase.getInstance().getReference("user").child(userId)
        userRef.get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            user?.let {
                profileImageUrl = it.profileImageUrl
            }
        }
        return profileImageUrl
    }

    private fun loadProfileImage() {
        val userRef = FirebaseDatabase.getInstance().getReference("user").child(userId)
        userRef.get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            user?.let {
                val profileImageUrl = it.profileImageUrl
                if (profileImageUrl.isNotEmpty()) {
                    // Glide를 사용해 URL을 바로 로드
                    Glide.with(this@ProfileActivity)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.profile_default)
                        .error(R.drawable.profile_default) // 오류 시 기본 이미지 표시
                        .into(binding.ivProfile)
                } else {
                    binding.ivProfile.setImageResource(R.drawable.profile_default)
                }
            }
        }.addOnFailureListener {
            binding.ivProfile.setImageResource(R.drawable.profile_default)
        }
    }

    private fun loadStatusMessage() {
        val userRef = FirebaseDatabase.getInstance().getReference("user").child(userId)
        userRef.get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            user?.let {
                val statusMessage = it.statusMessage
                if (!statusMessage.isNullOrEmpty()) {
                    binding.tvStatusMessage.text = statusMessage
                    binding.tvStatusMessage.visibility = View.VISIBLE
                } else {
                    binding.tvStatusMessage.visibility = View.GONE
                }
            }
        }.addOnFailureListener {
            binding.tvStatusMessage.visibility = View.GONE
        }
    }
}