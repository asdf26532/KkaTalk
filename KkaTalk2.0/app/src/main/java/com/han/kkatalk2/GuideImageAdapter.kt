package com.han.kkatalk2

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView

class GuideImageAdapter(
    private val imageList: List<String>
) : RecyclerView.Adapter<GuideImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(val photoView: PhotoView) : RecyclerView.ViewHolder(photoView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val photoView = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_full_screen_image, parent, false) as PhotoView
        return ImageViewHolder(photoView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Glide.with(holder.photoView.context)
            .load(imageList[position])
            .placeholder(R.drawable.image_default)
            .error(R.drawable.image_default)
            .into(holder.photoView)
    }

    override fun getItemCount() = imageList.size
}
