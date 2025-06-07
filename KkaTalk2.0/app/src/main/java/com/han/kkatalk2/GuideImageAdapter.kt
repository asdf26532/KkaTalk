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

    class ImageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val imageView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_pager, parent, false) as ImageView
        return ImageViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val url = imageList[position]
        Glide.with(holder.imageView.context)
            .load(url)
            .placeholder(R.drawable.image_default)
            .error(R.drawable.image_default)
            .into(holder.imageView)

        // 클릭 시 전체화면 보기
        holder.imageView.setOnClickListener {
            val context = holder.imageView.context
            val intent = FullScreenImageActivity.newIntent(context, imageList, position)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = imageList.size
}
