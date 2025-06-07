package com.han.kkatalk2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView

class FullScreenImageActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context, imageUrls: List<String>, position: Int): Intent {
            return Intent(context, FullScreenImageActivity::class.java).apply {
                putStringArrayListExtra("IMAGE_URLS", ArrayList(imageUrls))
                putExtra("START_POSITION", position)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        val imageUrls = intent.getStringArrayListExtra("IMAGE_URLS") ?: arrayListOf()
        val startPosition = intent.getIntExtra("START_POSITION", 0)

        val viewPager = findViewById<ViewPager2>(R.id.full_screen_view_pager)
        viewPager.adapter = GuideImageAdapter(imageUrls)
        viewPager.setCurrentItem(startPosition, false)
    }
}