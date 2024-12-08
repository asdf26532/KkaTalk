package com.han.kkaTalk

import android.content.Intent
import android.os.Bundle
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

        // Intent에서 사용자 UID와 닉네임 가져오기
        userId = intent.getStringExtra("uId") ?: ""
        userNick = intent.getStringExtra("nick") ?: ""

        // 닉네임 표시
        binding.tvNick.text = "닉네임: $userNick"

        // 사용자 프로필 이미지 로드
        loadProfileImage()

        // 대화하기 버튼 클릭 이벤트
        binding.btnChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("uId", userId)
            intent.putExtra("nick", userNick)
            startActivity(intent)
        }

        // 취소 버튼 클릭 이벤트
        binding.btnLogout.setOnClickListener {
            finish()
        }
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
}
