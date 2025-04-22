package com.han.kkatalk2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.han.kkatalk2.databinding.ActivityGuideDetailBinding

class GuideDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuideDetailBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var writerUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuideDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        // 현재 로그인한 사용자 UID 가져오기
        val currentUserUid = auth.currentUser?.uid

        // UI 요소 찾기
        val imgProfile = findViewById<ImageView>(R.id.img_profile)
        val txtName = findViewById<TextView>(R.id.txt_name)
        val txtLocation = findViewById<TextView>(R.id.txt_location)
        val txtRate = findViewById<TextView>(R.id.txt_rate)
        val txtPhone = findViewById<TextView>(R.id.txt_phone)
        val txtContent = findViewById<TextView>(R.id.txt_content)

        // 인텐트에서 가이드 정보 가져오기
        val guideId = intent.getStringExtra("guideId")
        val nick = intent.getStringExtra("nick")
        val profileImageUrl = intent.getStringExtra("profileImageUrl")

        // 받은 데이터 확인
        Log.d("GuideDetailActivity", "Received Data - guideId: $guideId, nick: $nick, profileImageUrl: $profileImageUrl")

        if (guideId.isNullOrEmpty()) {
            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            finish() // guideId가 없으면 액티비티 종료
            return
        }

        database = FirebaseDatabase.getInstance().getReference("guide").child(guideId)
        database.get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    showErrorAndExit("해당 가이드를 찾을 수 없습니다.")
                    return@addOnSuccessListener
                }

                val guide = snapshot.getValue(Guide::class.java)
                if (guide != null) {
                    writerUid = guide.uId  // 작성자 UID 가져오기

                    txtName.text = guide.title
                    txtLocation.text = "지역: ${guide.locate}"
                    txtRate.text = "요금: ${guide.rate}"
                    txtPhone.text = "전화번호: ${guide.phoneNumber}"
                    txtContent.text = guide.content

                    // 프로필 이미지 로드
                    if (!guide.profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(guide.profileImageUrl).into(imgProfile)
                    } else {
                        imgProfile.setImageResource(R.drawable.profile_default)
                    }

                    Log.d("GuideDetailActivity", "Current User ID: $currentUserUid, Writer ID: $writerUid")

                    // 본인이 작성한 글이면 "수정하기", 아니면 "대화하기"
                    if (currentUserUid == writerUid) {
                        Log.d("GuideDetailActivity", "This is the user's own post. Showing 'Edit' button.")
                        binding.btnChat.text = "수정하기"
                        binding.btnChat.setOnClickListener {
                            val intent = Intent(this, RegisterGuideActivity::class.java)
                            intent.putExtra("guideId", guideId)
                            startActivity(intent)
                        }
                    } else {
                        Log.d("GuideDetailActivity", "This is another user's post. Showing 'Chat' button.")
                        binding.btnChat.text = "대화하기"
                        binding.btnChat.setOnClickListener {
                            val intent = Intent(this, ChatActivity::class.java).apply {
                                putExtra("uId", guideId)
                                putExtra("nick", nick)
                                putExtra("profileImageUrl", profileImageUrl)
                            }
                            Log.d("GuideDetailActivity", "uId: $guideId, nick: $nick, profileImageUrl: $profileImageUrl")
                            startActivity(intent)
                        }
                    }

                } else {
                    showErrorAndExit("데이터를 불러오는 중 오류가 발생했습니다.")
                }
            }
            .addOnFailureListener { exception ->
                showErrorAndExit("네트워크 오류가 발생했습니다. 다시 시도해주세요.")
            }

        // 프로필 사진 클릭
        imgProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("uId", guideId)
            intent.putExtra("nick", nick)
            intent.putExtra("profileImageUrl", profileImageUrl)
            startActivity(intent)
        }

        // 액션바 설정
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // 오류 발생 시 Toast 메시지를 띄우고 액티비티 종료
    private fun showErrorAndExit(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }

    // 버튼(옵션) 선택
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent()
                intent.putExtra("chatUpdated", true) // 결과 값으로 '갱신 필요' 플래그 전달
                setResult(Activity.RESULT_OK, intent)
                Log.d("ChatActivity", "setResult 호출됨") // 로그 추가
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

}
