package com.han.kkatalk2

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class GuideImageAdapter(
    private val imageList: List<Any>,
    private val isDrawable: Boolean = false
) : RecyclerView.Adapter<GuideImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val imageView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_pager, parent, false) as ImageView
        return ImageViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        if (isDrawable) {
            val resId = imageList[position] as Int
            holder.imageView.setImageResource(resId)
        } else {
            val url = imageList[position] as String
            Glide.with(holder.imageView.context)
                .load(url)
                .placeholder(R.drawable.image_default)
                .error(R.drawable.image_default)
                .into(holder.imageView)
        }
    }

    override fun getItemCount() = imageList.size
}
