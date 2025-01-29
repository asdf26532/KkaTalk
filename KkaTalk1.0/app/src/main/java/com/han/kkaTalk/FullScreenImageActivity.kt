package com.han.kkaTalk

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView

class FullScreenImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        val photoView: PhotoView = findViewById(R.id.photo_view)

        // Intent로 전달된 이미지 URL 받기
        val imageUrl = intent.getStringExtra("IMAGE_URL")

        // Glide를 사용하여 이미지를 PhotoView에 로드
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery) // 로딩 중 표시할 기본 이미지
                .error(android.R.drawable.ic_delete) // 실패 시 표시할 기본 이미지
                .into(photoView)
        }
    }

}