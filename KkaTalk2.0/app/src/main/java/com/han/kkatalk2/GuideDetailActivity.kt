package com.han.kkatalk2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.storage.FirebaseStorage
import com.han.kkatalk2.databinding.ActivityGuideDetailBinding

class GuideDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuideDetailBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var writerUid: String? = null

    private lateinit var viewPager: ViewPager2
    private lateinit var imageAdapter: GuideImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuideDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val currentUserUid = auth.currentUser?.uid

        val txtTitle = findViewById<TextView>(R.id.txt_title)
        val imgProfile = findViewById<ImageView>(R.id.img_profile)
        val txtNick = findViewById<TextView>(R.id.txt_name)
        val txtLocation = findViewById<TextView>(R.id.txt_location)
        val txtRate = findViewById<TextView>(R.id.txt_rate)
        val txtPhone = findViewById<TextView>(R.id.txt_phone)
        val txtContent = findViewById<TextView>(R.id.txt_content)
        val txtViewCount = findViewById<TextView>(R.id.txt_view_count)
        val indicator = findViewById<me.relex.circleindicator.CircleIndicator3>(R.id.indicator)

        viewPager = findViewById(R.id.view_pager)


        val guideId = intent.getStringExtra("guideId")
        val nick = intent.getStringExtra("nick")
        val profileImageUrl = intent.getStringExtra("profileImageUrl")

        if (guideId.isNullOrEmpty()) {
            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        database = FirebaseDatabase.getInstance().getReference("guide").child(guideId)

        database.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val guide = currentData.getValue(Guide::class.java) ?: return Transaction.success(currentData)
                val updatedGuide = guide.copy(viewCount = guide.viewCount + 1)
                currentData.value = updatedGuide
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null) {
                    Log.e("GuideDetailActivity", "조회수 증가 실패: ${error.message}")
                } else {
                    Log.d("GuideDetailActivity", "조회수 증가 성공")
                }
            }
        })

        database.get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    showErrorAndExit("해당 가이드를 찾을 수 없습니다.")
                    return@addOnSuccessListener
                }

                val guide = snapshot.getValue(Guide::class.java)
                if (guide != null) {
                    writerUid = guide.uId

                    txtTitle.text = guide.title
                    txtNick.text = guide.nick
                    txtLocation.text = "지역: ${guide.locate}"
                    txtRate.text = "요금: ${guide.rate}"
                    txtPhone.text = "전화번호: ${guide.phoneNumber}"
                    txtContent.text = guide.content
                    txtViewCount.text = "조회수: ${guide.viewCount}"

                    if (!guide.profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(guide.profileImageUrl)
                            .placeholder(R.drawable.profile_default)
                            .error(R.drawable.profile_default)
                            .into(imgProfile)
                    } else {
                        imgProfile.setImageResource(R.drawable.profile_default)
                    }

                    val imageUrls = guide.imageUrls
                    if (imageUrls.isNullOrEmpty()) {
                        // 없으면 기본이미지 하나 넣기
                        imageAdapter = GuideImageAdapter(listOf(R.drawable.image_default), true)
                    } else {
                        imageAdapter = GuideImageAdapter(imageUrls)
                    }
                    viewPager.adapter = imageAdapter
                    indicator.setViewPager(viewPager)

                    // 버튼 처리
                    if (currentUserUid == writerUid) {
                        binding.btnChat.text = "수정하기"
                        binding.btnChat.setOnClickListener {
                            val intent = Intent(this, RegisterGuideActivity::class.java)
                            intent.putExtra("guideId", guideId)
                            startActivity(intent)
                        }
                    } else {
                        binding.btnChat.text = "대화하기"
                        binding.btnChat.setOnClickListener {
                            val intent = Intent(this, ChatActivity::class.java).apply {
                                putExtra("uId", guideId)
                                putExtra("nick", nick)
                                putExtra("profileImageUrl", profileImageUrl)
                            }
                            startActivity(intent)
                        }
                    }
                } else {
                    showErrorAndExit("데이터를 불러오는 중 오류가 발생했습니다.")
                }
            }
            .addOnFailureListener {
                showErrorAndExit("네트워크 오류가 발생했습니다. 다시 시도해주세요.")
            }

        imgProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("uId", guideId)
            intent.putExtra("nick", nick)
            intent.putExtra("profileImageUrl", profileImageUrl)
            startActivity(intent)
        }

        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun showErrorAndExit(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.guide_detail_menu, menu)

        val currentUserUid = auth.currentUser?.uid

        if (currentUserUid == writerUid) {
            menu?.findItem(R.id.action_edit)?.isVisible = true
            menu?.findItem(R.id.action_delete)?.isVisible = true
            menu?.findItem(R.id.action_bump)?.isVisible = true
        } else {
            menu?.findItem(R.id.action_report)?.isVisible = true
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent()
                setResult(Activity.RESULT_OK, intent)
                finish()
                true
            }

            R.id.action_edit -> {
                Toast.makeText(this, "수정 클릭됨", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.action_delete -> {
                Toast.makeText(this, "삭제 클릭됨", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.action_bump -> {
                Toast.makeText(this, "끌어올리기 클릭됨", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.action_report -> {
                Toast.makeText(this, "신고하기 클릭됨", Toast.LENGTH_SHORT).show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
