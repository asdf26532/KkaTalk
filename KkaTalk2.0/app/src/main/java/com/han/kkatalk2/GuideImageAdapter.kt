package com.han.kkatalk2

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class GuideImageAdapter(
    private val imageList: List<String>,
    private val onImageClick: ((position: Int) -> Unit)? = null
) : RecyclerView.Adapter<GuideImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        Log.d("GuideImageAdapter", "onCreateViewHolder 호출됨")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_pager, parent, false)

        val imageView = view.findViewById<ImageView>(R.id.image_view)
        return ImageViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val url = imageList[position]
        Glide.with(holder.imageView.context)
            .load(url)
            .placeholder(R.drawable.image_default)
            .error(R.drawable.image_default)
            .into(holder.imageView)

        // 외부에서 클릭 리스너가 주어졌을 때만 실행
        holder.imageView.setOnClickListener {
            onImageClick?.invoke(position)
        }
    }

    override fun getItemCount(): Int {
        Log.d("GuideImageAdapter", "getItemCount() = ${imageList.size}")
        return imageList.size
    }
}
