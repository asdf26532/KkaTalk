package com.han.kkatalk2

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class GuideImageAdapter(
    private val imageList: List<String>,
    private val onImageClick: ((position: Int, url: String) -> Unit)? = null
) : RecyclerView.Adapter<GuideImageAdapter.ImageViewHolder>() {

    // ViewHolder: 뷰 전체를 받고, 내부에서 imageView를 찾음
    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        Log.d("GuideImageAdapter", "onCreateViewHolder 호출됨")

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_pager, parent, false)

        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Log.d("GuideImageAdapter", "onBindViewHolder position: $position")
        val url = imageList[position]

        Glide.with(holder.imageView.context)
            .load(url)
            .placeholder(R.drawable.image_default)
            .error(R.drawable.image_default)
            .centerCrop()
            .into(holder.imageView)

        holder.imageView.setOnClickListener {
            onImageClick?.invoke(position, url)
        }
    }

    override fun getItemCount(): Int {
        if (BuildConfig.DEBUG) {
            Log.d("GuideImageAdapter", "getItemCount() = ${imageList.size}")
        }
        return imageList.size
    }
}
