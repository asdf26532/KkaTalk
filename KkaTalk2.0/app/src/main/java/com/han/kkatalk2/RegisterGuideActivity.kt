package com.han.kkatalk2

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class RegisterGuideActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var guideDatabase: DatabaseReference
    private lateinit var userDatabase: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var prefs: SharedPreferences

    private lateinit var imageContainer: LinearLayout
    private lateinit var imgAdd: ImageView
    private val selectedImageUris = mutableListOf<Uri>()

    private var guideId: String? = null
    private val uploadedUrls = mutableListOf<String>()

    private lateinit var userId: String
    private lateinit var txtImageCount: TextView
    private val MAX_IMAGE_COUNT = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_guide)

        auth = FirebaseAuth.getInstance()
        guideDatabase = FirebaseDatabase.getInstance().getReference("guide")
        userDatabase = FirebaseDatabase.getInstance().getReference("user")
        storage = FirebaseStorage.getInstance(BuildConfig.STORAGE_BUCKET)
        prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        val edtTitle = findViewById<EditText>(R.id.edt_title)
        val spinnerLocation = findViewById<Spinner>(R.id.spinner_location)
        val edtRate = findViewById<EditText>(R.id.edt_rate)
        val edtPhone = findViewById<EditText>(R.id.edt_phone)
        val edtContent = findViewById<EditText>(R.id.edt_content)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val btnBack = findViewById<Button>(R.id.btn_back)

        txtImageCount = findViewById(R.id.txt_image_count)
        imageContainer = findViewById(R.id.img_container)
        imgAdd = findViewById(R.id.img_add)

        ArrayAdapter.createFromResource(
            this,
            R.array.city_list,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerLocation.adapter = adapter
        }

        userId = auth.currentUser?.uid
            ?: prefs.getString("userId", null).orEmpty()

        if (userId.isEmpty()) {
            Log.e("RegisterGuide", "현재 사용자 ID를 찾을 수 없습니다.")
            finish()
            return
        }


        guideId = intent.getStringExtra("guideId")
        if (guideId != null) {
            loadGuideData(guideId!!, edtTitle, spinnerLocation, edtRate, edtPhone, edtContent, btnRegister)
        }

        imgAdd.setOnClickListener {
            if (selectedImageUris.size >= MAX_IMAGE_COUNT) {
                showCustomToast("사진은 최대 10장까지 선택 가능합니다.")
                return@setOnClickListener
            }
            ImagePicker.with(this)
                .galleryOnly()
                .maxResultSize(1024, 1024)
                .start(101) // requestCode 직접 설정
        }

        userDatabase.child(userId).get().addOnSuccessListener { snapshot ->
            val nick = snapshot.child("nick").value as? String ?: ""
            val profileImageUrl = snapshot.child("profileImageUrl").value as? String ?: ""
            Log.d("RegisterGuide", "닉네임: $nick / 프로필 URL: $profileImageUrl")

            btnRegister.setOnClickListener {
                val title = edtTitle.text.toString()
                val location = spinnerLocation.selectedItem.toString()
                val rate = edtRate.text.toString()
                val phone = edtPhone.text.toString()
                val content = edtContent.text.toString()

                Log.d("RegisterGuide", "입력값 - name: $title, location: $location, rate: $rate, phone: $phone, content: $content")

                if (title.isNotEmpty() && location.isNotEmpty() && rate.isNotEmpty() && phone.isNotEmpty()) {
                    val progressDialog = ProgressDialog(this).apply {
                        setMessage("이미지 업로드 중...")
                        setCancelable(false)
                        show()
                    }
                    if (selectedImageUris.isNotEmpty()) {
                        uploadedUrls.clear()
                        Log.d("RegisterGuide", "선택된 이미지 수: ${selectedImageUris.size}")
                        uploadImagesToFirebase(userId, progressDialog) {
                            Log.d("RegisterGuide", "이미지 업로드 완료, URL 목록: $uploadedUrls")
                            registerGuide(title, userId, nick, phone, location, rate, content, profileImageUrl, uploadedUrls)
                        }
                    } else {
                        Log.d("RegisterGuide", "이미지 없이 가이드 등록 시도")
                        registerGuide(title, userId, nick, phone, location, rate, content, profileImageUrl, listOf())
                        progressDialog.dismiss()
                    }
                } else {
                    showCustomToast("모든 칸을 입력해주세요.")
                }
            }
        }.addOnFailureListener {
            Log.e("RegisterGuide", "유저 정보 로드 실패: ${it.message}")
            showCustomToast("유저 정보를 불러오지 못했습니다.")
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun uploadImagesToFirebase(userId: String, progressDialog: ProgressDialog, onComplete: () -> Unit) {
        var uploadCount = 0
        uploadedUrls.clear()

        Log.d("RegisterGuide", "업로드 경로: guide_images/$userId/... / 현재 로그인 UID: ${auth.currentUser?.uid}")

        for ((index, uri) in selectedImageUris.withIndex()) {
            val fileName = "guide_images/$userId/${System.currentTimeMillis()}_$index.jpg"
            val storageRef = storage.reference.child(fileName)

            storageRef.putFile(uri)
                .addOnSuccessListener {
                    Log.d("RegisterGuide", "업로드 성공: $fileName")
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        Log.d("RegisterGuide", "다운로드 URL: $downloadUrl")
                        uploadedUrls.add(downloadUrl.toString())
                        uploadCount++
                        if (uploadCount == selectedImageUris.size) {
                            progressDialog.dismiss()
                            onComplete()
                        }
                    }.addOnFailureListener {
                        Log.e("RegisterGuide", "다운로드 URL 가져오기 실패: ${it.message}")
                        progressDialog.dismiss()
                    }
                }
                .addOnFailureListener {
                    Log.e("RegisterGuide", "이미지 업로드 실패: ${it.message}")
                    progressDialog.dismiss()
                }
        }
    }

    private fun registerGuide(
        title: String,
        userId: String,
        nick: String,
        phone: String,
        location: String,
        rate: String,
        content: String,
        profileImageUrl: String,
        imageUrls: List<String>
    ) {
        val guideRef = guideDatabase.child(userId)
        val timestamp = System.currentTimeMillis()

        guideRef.child("viewCount").get().addOnSuccessListener { snapshot ->
            val currentViewCount = snapshot.getValue(Int::class.java) ?: 0

            val guide = Guide(title, userId, nick, phone, location, rate, content, profileImageUrl, imageUrls, currentViewCount, timestamp)
            Log.d("RegisterGuide", "파이어베이스에 등록할 Guide 객체: $guide")

            guideRef.setValue(guide).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("RegisterGuide", "등록 성공")
                    showCustomToast("등록 완료!")
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Log.e("RegisterGuide", "등록 실패: ${it.exception?.message}")
                    showCustomToast("등록 실패 : ${it.exception?.message} ")
                }
            }

        }.addOnFailureListener {
            showCustomToast("viewCount 불러오기 실패")
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("RegisterGuide", "onActivityResult 호출됨: requestCode=$requestCode, resultCode=$resultCode")

        if (resultCode == RESULT_OK && requestCode == 101) {
            try {
                if (selectedImageUris.size >= 10) {
                    showCustomToast("최대 10장까지 업로드할 수 있습니다.")
                    Log.w("RegisterGuide", "이미 10장 이상 선택됨")
                    return
                }

                if (data == null) {
                    Log.e("RegisterGuide", "data가 null입니다.")
                    return
                }

                val uri: Uri = data.data ?: run {
                    Log.e("RegisterGuide", "data.data가 null입니다.")
                    return
                }

                Log.d("RegisterGuide", "이미지 URI 확인: $uri")

                selectedImageUris.add(uri)
                Log.d("RegisterGuide", "현재 이미지 수: ${selectedImageUris.size}")

                displaySelectedImages()

            } catch (e: Exception) {
                Log.e("RegisterGuide", "이미지 처리 중 예외 발생: ${e.message}", e)
            }
        }
    }

    private fun displaySelectedImages() {
        val totalChildren = imageContainer.childCount
        if (totalChildren > 1) {
            imageContainer.removeViews(1, totalChildren - 1)
        }

        for ((index, uri) in selectedImageUris.withIndex()) {
            val frame = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(250, 250).apply {
                    setMargins(8, 8, 8, 8)
                }
            }

            val imageView = ImageView(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            Glide.with(this).load(uri).into(imageView)

            val deleteBtn = ImageView(this).apply {
                setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                layoutParams = FrameLayout.LayoutParams(60, 60).apply {
                    marginEnd = 4
                    topMargin = 4
                    gravity = Gravity.END or Gravity.TOP
                }
                setOnClickListener {
                    selectedImageUris.removeAt(index)
                    displaySelectedImages() // 다시 갱신
                }
            }

            frame.addView(imageView)
            frame.addView(deleteBtn)
            imageContainer.addView(frame)
        }

        txtImageCount.text = "${selectedImageUris.size}/10"
    }

    private fun loadGuideData(
        guideId: String,
        edtName: EditText,
        spinnerLocation: Spinner,
        edtRate: EditText,
        edtPhone: EditText,
        edtContent: EditText,
        btnRegister: Button
    ) {
        guideDatabase.child(guideId).get().addOnSuccessListener { snapshot ->
            val guide = snapshot.getValue(Guide::class.java)
            val cityList = resources.getStringArray(R.array.city_list)
            if (guide != null) {
                edtName.setText(guide.title)
                edtRate.setText(guide.rate)
                edtPhone.setText(guide.phoneNumber)
                edtContent.setText(guide.content)

                val index = cityList.indexOf(guide.locate)
                if (index >= 0) {
                    spinnerLocation.setSelection(index)
                }

                selectedImageUris.clear()
                selectedImageUris.addAll((guide.imageUrls ?: emptyList()).map { Uri.parse(it) })
                displaySelectedImages()

                btnRegister.text = "수정하기"
                Log.d("RegisterGuide", "기존 가이드 정보 로딩 완료")
            }
        }.addOnFailureListener {
            Log.e("RegisterGuide", "가이드 데이터 로드 실패: ${it.message}")
            showCustomToast("가이드 정보를 불러오지 못했습니다")
        }
    }
}
