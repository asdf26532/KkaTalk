package com.han.kkatalk2

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class GuideDetailActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide_detail)

        // UI 요소 찾기
        val imgProfile = findViewById<ImageView>(R.id.img_profile)
        val txtName = findViewById<TextView>(R.id.txt_name)
        val txtLocation = findViewById<TextView>(R.id.txt_location)
        val txtRate = findViewById<TextView>(R.id.txt_rate)
        val txtPhone = findViewById<TextView>(R.id.txt_phone)
        val txtContent = findViewById<TextView>(R.id.txt_content)

        // 인텐트에서 가이드 정보 가져오기
        val guideId = intent.getStringExtra("guideId") ?: return

        database = FirebaseDatabase.getInstance().getReference("guide").child(guideId)

        database.get().addOnSuccessListener { snapshot ->
            val guide = snapshot.getValue(Guide::class.java)
            if (guide != null) {
                txtName.text = guide.name
                txtLocation.text = "지역: ${guide.locate}"
                txtRate.text = "요금: ${guide.rate}"
                txtPhone.text = "전화번호: ${guide.phoneNumber}"
                txtContent.text = guide.content

                // 프로필 이미지가 있다면 Glide로 로드
                if (guide.profileImageUrl.isNotEmpty()) {
                    Glide.with(this).load(guide.profileImageUrl).into(imgProfile)
                } else {
                    imgProfile.setImageResource(R.drawable.profile_default)
                }
            }
        }
    }
}
