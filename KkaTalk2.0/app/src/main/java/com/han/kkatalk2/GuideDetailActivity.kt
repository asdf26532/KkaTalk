package com.han.kkatalk2

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator


class GuideDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuideDetailBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var prefs: SharedPreferences
    private var writerUid: String? = null

    private lateinit var viewPager: ViewPager2
    private lateinit var dotsIndicator: WormDotsIndicator
    private lateinit var imageAdapter: GuideImageAdapter

    private lateinit var guideId: String
    private var currentGuide: Guide? = null

    companion object {
        private const val REQUEST_EDIT_GUIDE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuideDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        guideId = intent.getStringExtra("guideId")
            ?: throw IllegalArgumentException("guideId가 존재하지 않습니다.")

        val nick = intent.getStringExtra("nick")
        val profileImageUrl = intent.getStringExtra("profileImageUrl")

        if (guideId.isEmpty()) {
            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        database = FirebaseDatabase.getInstance().getReference("guide").child(guideId)
        viewPager = findViewById(R.id.view_pager)
        dotsIndicator = findViewById(R.id.dots_indicator)

        // 가이드 정보 불러오기 및 조회수 증가
        loadGuide()

        // 프로필 클릭 시 프로필 화면으로 이동
        findViewById<ImageView>(R.id.img_profile).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("uId", guideId)
            intent.putExtra("nick", nick)
            intent.putExtra("profileImageUrl", profileImageUrl)
            startActivity(intent)
        }

        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun loadGuide() {

        val currentUserUid = auth.currentUser?.uid
            ?: prefs.getString("userId", null).orEmpty()

        if (currentUserUid.isEmpty()) {
            Log.e("RegisterGuide", "현재 사용자 ID를 찾을 수 없습니다.")
            finish()
            return
        }

        // 가이드 정보 가져오기
        database.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                showErrorAndExit("해당 가이드를 찾을 수 없습니다.")
                return@addOnSuccessListener
            }

            val guide = snapshot.getValue(Guide::class.java)
            if (guide != null) {
                currentGuide = guide
                writerUid = guide.uId

                updateUI(guide)

                // 버튼 처리
                if (currentUserUid == writerUid) {
                    binding.btnChat.text = "수정하기"
                    binding.btnChat.setOnClickListener {
                        val intent = Intent(this, RegisterGuideActivity::class.java)
                        intent.putExtra("guideId", guideId)
                        startActivityForResult(intent, REQUEST_EDIT_GUIDE)
                    }
                } else {
                    binding.btnChat.text = "대화하기"
                    binding.btnChat.setOnClickListener {
                        val intent = Intent(this, ChatActivity::class.java).apply {
                            putExtra("uId", guideId)
                            putExtra("nick", guide.nick)
                            putExtra("profileImageUrl", guide.profileImageUrl)
                        }
                        startActivity(intent)
                    }
                }

                // 조회수 증가 처리
                if (currentUserUid != null && currentUserUid != guide.uId) {
                    val viewsRef = database.child("views").child(currentUserUid)

                    viewsRef.get().addOnSuccessListener { viewSnapshot ->
                        if (!viewSnapshot.exists()) {
                            // 처음 보는 사용자 -> 조회수 증가
                            database.runTransaction(object : Transaction.Handler {
                                override fun doTransaction(currentData: MutableData): Transaction.Result {
                                    val currentGuide = currentData.getValue(Guide::class.java)
                                        ?: return Transaction.success(currentData)

                                    val updatedGuide = currentGuide.copy(viewCount = currentGuide.viewCount + 1)
                                    currentData.value = updatedGuide
                                    return Transaction.success(currentData)
                                }

                                override fun onComplete(
                                    error: DatabaseError?,
                                    committed: Boolean,
                                    currentData: DataSnapshot?
                                ) {
                                    if (error != null) {
                                        Log.e("GuideDetailActivity", "조회수 증가 실패: ${error.message}")
                                    } else {
                                        Log.d("GuideDetailActivity", "조회수 증가 성공")
                                        // 기록 남기기
                                        viewsRef.setValue(true)
                                    }
                                }
                            })
                        } else {
                            Log.d("GuideDetailActivity", "이미 조회한 사용자 - 조회수 증가 없음")
                        }
                    }
                }

            } else {
                showErrorAndExit("데이터를 불러오는 중 오류가 발생했습니다.")
            }
        }.addOnFailureListener {
            showErrorAndExit("네트워크 오류가 발생했습니다. 다시 시도해주세요.")
        }
    }

    private fun updateUI(guide: Guide) {
        findViewById<TextView>(R.id.txt_title).text = guide.title
        findViewById<TextView>(R.id.txt_name).text = guide.nick
        findViewById<TextView>(R.id.txt_location).text = "지역: ${guide.locate}"
        findViewById<TextView>(R.id.txt_rate).text = "요금: ${guide.rate}"
        findViewById<TextView>(R.id.txt_phone).text = "전화번호: ${guide.phoneNumber}"
        findViewById<TextView>(R.id.txt_content).text = guide.content
        findViewById<TextView>(R.id.txt_view_count).text = "조회수: ${guide.viewCount}"

        val imgProfile = findViewById<ImageView>(R.id.img_profile)
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
        Log.d("GuideDetailActivity", "Guide 이미지 리스트 크기: ${imageUrls.size}")
        imageUrls.forEach {
            Log.d("GuideDetailActivity", "이미지 URL: $it")
        }
             imageAdapter = if (imageUrls.isEmpty()) {
            val defaultImageUri = "android.resource://${packageName}/${R.drawable.image_default}"
            GuideImageAdapter(listOf(defaultImageUri))
        } else {
            GuideImageAdapter(imageUrls) { position, _ ->
                val intent = FullScreenImageActivity.newIntent(this, imageUrls, position)
                startActivity(intent)
            }
        }
        viewPager.adapter = imageAdapter
        viewPager.post {
            dotsIndicator.attachTo(viewPager)
            Log.d("CHECK", "Adapter item count: ${imageAdapter.itemCount}")
        }

    }

    private fun showErrorAndExit(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }

    // 수정 후 새로고침
    private fun reloadGuideData() {
        database.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                showErrorAndExit("해당 가이드를 찾을 수 없습니다.")
                return@addOnSuccessListener
            }

            val guide = snapshot.getValue(Guide::class.java)
            if (guide != null) {
                currentGuide = guide
                updateUI(guide)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_EDIT_GUIDE && resultCode == RESULT_OK) {
            reloadGuideData()
        }
    }

    // 메뉴 관련 메서드
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.guide_detail_menu, menu)

        val currentUserUid = auth.currentUser?.uid
            ?: prefs.getString("userId", null)

        if (currentUserUid == writerUid) {
            menu?.findItem(R.id.action_delete)?.isVisible = true
            menu?.findItem(R.id.action_bump)?.isVisible = true
        } else {
            menu?.findItem(R.id.action_report)?.isVisible = true
        }

        return true
    }

    // 버튼처리
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent()
                setResult(Activity.RESULT_OK, intent)
                finish()
                true
            }

            R.id.action_delete -> {
                AlertDialog.Builder(this)
                    .setTitle("삭제 확인")
                    .setMessage("정말 삭제하시겠습니까?")
                    .setPositiveButton("삭제") { _, _ ->
                        currentGuide?.let {
                            deleteGuide(guideId, it.imageUrls ?: emptyList())
                        } ?: Toast.makeText(this, "가이드 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("취소", null)
                    .show()
                true
            }

            R.id.action_bump -> {
                AlertDialog.Builder(this)
                    .setTitle("끌어올리기 확인")
                    .setMessage("글을 끌어올리겠습니까?")
                    .setPositiveButton("확인") { _, _ ->
                        liftGuide()
                    }
                    .setNegativeButton("취소", null)
                    .show()
                true
            }

            R.id.action_report -> {
                Toast.makeText(this, "신고하기 클릭됨", Toast.LENGTH_SHORT).show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    // 게시글 지우기
    private fun deleteGuide(guideId: String, imageUrls: List<String>) {
        val storage = FirebaseStorage.getInstance(BuildConfig.STORAGE_BUCKET)

        imageUrls.forEach { url ->
            storage.getReferenceFromUrl(url)
                .delete()
                .addOnSuccessListener {
                    Log.d("GuideDelete", "이미지 삭제 성공: $url")
                }
                .addOnFailureListener {
                    Log.w("GuideDelete", "이미지 삭제 실패: $url", it)
                }
        }

        FirebaseDatabase.getInstance()
            .getReference("guide")
            .child(guideId)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "삭제 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 게시글 끌어올리기
    private fun liftGuide() {

        val guideRef = FirebaseDatabase.getInstance().getReference("guide").child(guideId)

        guideRef.get().addOnSuccessListener { snapshot ->
            val guide = snapshot.getValue(Guide::class.java)
            if (guide == null) {
                Toast.makeText(this, "가이드 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            val lastTimestamp = guide.timestamp
            val currentTime = System.currentTimeMillis()
            val twentyFourHoursMillis = 24 * 60 * 60 * 1000L

            if (currentTime - lastTimestamp < twentyFourHoursMillis) {
                val remaining = twentyFourHoursMillis - (currentTime - lastTimestamp)
                val hours = remaining / (60 * 60 * 1000)
                val minutes = (remaining % (60 * 60 * 1000)) / (60 * 1000)
                Toast.makeText(this, "끌어올리기는 24시간에 한 번만 가능합니다.\n남은 시간: ${hours}시간 ${minutes}분", Toast.LENGTH_LONG).show()
                return@addOnSuccessListener
            }

            // timestamp 업데이트
            guideRef.child("timestamp").setValue(currentTime)
                .addOnSuccessListener {
                    Toast.makeText(this, "끌어올리기가 완료되었습니다!", Toast.LENGTH_SHORT).show()
                    reloadGuideData()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "끌어올리기 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }

        }.addOnFailureListener {
            Toast.makeText(this, "데이터 불러오기 실패: ${it.message}", Toast.LENGTH_SHORT).show()
        }


    }
}