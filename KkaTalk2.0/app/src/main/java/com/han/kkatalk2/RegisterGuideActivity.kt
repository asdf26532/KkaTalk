package com.han.kkatalk2

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.File

class RegisterGuideActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var guideDatabase: DatabaseReference
    private lateinit var userDatabase: DatabaseReference
    private lateinit var storage: FirebaseStorage

    private lateinit var imageContainer: LinearLayout
    private lateinit var btnAddImages: Button
    private val selectedImageUris = mutableListOf<Uri>()

    private var guideId: String? = null
    private val uploadedUrls = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_guide)

        auth = FirebaseAuth.getInstance()
        guideDatabase = FirebaseDatabase.getInstance().getReference("guide")
        userDatabase = FirebaseDatabase.getInstance().getReference("user")
        storage = FirebaseStorage.getInstance("gs://kkatalk-cf3fd.appspot.com")

        val edtName = findViewById<EditText>(R.id.edt_name)
        val spinnerLocation = findViewById<Spinner>(R.id.spinner_location)
        val edtRate = findViewById<EditText>(R.id.edt_rate)
        val edtPhone = findViewById<EditText>(R.id.edt_phone)
        val edtContent = findViewById<EditText>(R.id.edt_content)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val btnBack = findViewById<Button>(R.id.btn_back)

        imageContainer = findViewById(R.id.image_container)
        btnAddImages = findViewById(R.id.btn_add_images)

        ArrayAdapter.createFromResource(
            this,
            R.array.city_list,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerLocation.adapter = adapter
        }

        // 로그인 상태 체크
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val userId = auth.currentUser?.uid ?: return

        guideId = intent.getStringExtra("guideId")
        if (guideId != null) {
            loadGuideData(guideId!!, edtName, spinnerLocation, edtRate, edtPhone, edtContent, btnRegister)
        }

        btnAddImages.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .maxResultSize(1080, 1080)
                .galleryMimeTypes(arrayOf("image/*"))
                .createIntent { intent ->
                    startActivityForResult(intent, 101)
                }
        }

        userDatabase.child(userId).get().addOnSuccessListener { snapshot ->
            val nick = snapshot.child("nick").value as? String ?: ""
            val profileImageUrl = snapshot.child("profileImageUrl").value as? String ?: "" // ✅ 프로필 이미지 URL 가져오기
            Log.d("RegisterGuide", "닉네임: $nick / 프로필 URL: $profileImageUrl")

            btnRegister.setOnClickListener {
                val name = edtName.text.toString()
                val location = spinnerLocation.selectedItem.toString()
                val rate = edtRate.text.toString()
                val phone = edtPhone.text.toString()
                val content = edtContent.text.toString()

                Log.d("RegisterGuide", "입력값 - name: $name, location: $location, rate: $rate, phone: $phone, content: $content")

                if (name.isNotEmpty() && location.isNotEmpty() && rate.isNotEmpty() && phone.isNotEmpty()) {
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
                            registerGuide(name, userId, nick, phone, location, rate, content, profileImageUrl, uploadedUrls)
                        }
                    } else {
                        Log.d("RegisterGuide", "이미지 없이 가이드 등록 시도")
                        registerGuide(name, userId, nick, phone, location, rate, content, profileImageUrl, listOf())
                        progressDialog.dismiss()
                    }
                } else {
                    Toast.makeText(this, "모든 필드를 입력하세요!", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener {
            Log.e("RegisterGuide", "유저 정보 로드 실패: ${it.message}")
            Toast.makeText(this, "유저 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
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
        name: String,
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
        val guide = Guide(name, userId, nick, phone, location, rate, content, profileImageUrl, imageUrls)
        Log.d("RegisterGuide", "파이어베이스에 등록할 Guide 객체: $guide")

        guideRef.setValue(guide).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("RegisterGuide", "가이드 등록 성공")
                Toast.makeText(this, "가이드 등록 완료!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Log.e("RegisterGuide", "가이드 등록 실패: ${it.exception?.message}")
                Toast.makeText(this, "등록 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 101) {
            val uri: Uri = data?.data ?: return
            Log.d("RegisterGuide", "이미지 선택됨: $uri")
            selectedImageUris.add(uri)
            displaySelectedImages()
        }
    }

    private fun displaySelectedImages() {
        imageContainer.removeAllViews()
        for (uri in selectedImageUris) {
            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(300, 300).apply {
                    setMargins(8, 0, 8, 0)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            Glide.with(this).load(uri).into(imageView)
            imageContainer.addView(imageView)
        }
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
                edtName.setText(guide.name)
                edtRate.setText(guide.rate)
                edtPhone.setText(guide.phoneNumber)
                edtContent.setText(guide.content)

                val index = cityList.indexOf(guide.locate)
                if (index >= 0) {
                    spinnerLocation.setSelection(index)
                }

                btnRegister.text = "수정하기"
                Log.d("RegisterGuide", "기존 가이드 정보 로딩 완료")
            }
        }.addOnFailureListener {
            Log.e("RegisterGuide", "가이드 데이터 로드 실패: ${it.message}")
            Toast.makeText(this, "가이드 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
