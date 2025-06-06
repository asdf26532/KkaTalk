package com.han.kkatalk2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView

class FullScreenImageActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var imageUrls: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        viewPager = findViewById(R.id.viewPager)

        imageUrls = intent.getStringArrayListExtra("IMAGE_URLS") ?: listOf()
        val startPosition = intent.getIntExtra("START_POSITION", 0)

        viewPager.adapter = GuideImageAdapter(imageUrls)
        viewPager.setCurrentItem(startPosition, false)
    }
}