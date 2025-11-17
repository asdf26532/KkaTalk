package com.han.reservation

import android.content.Intent
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class GuideAdapter(
    private val guideList: List<Guide>,
) : RecyclerView.Adapter<GuideAdapter.GuideViewHolder>() {

    private var highlightedQuery: String = ""

    // ViewHolder 클래스 정의
    class GuideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val guideImage: ImageView = itemView.findViewById(R.id.guideImage)
        val guideTitle: TextView = itemView.findViewById(R.id.guideTitle)
        val guideLocation: TextView = itemView.findViewById(R.id.guideLocation)
        val guideRate: TextView = itemView.findViewById(R.id.guideRate)
        val guideViewCount = itemView.findViewById<TextView>(R.id.txt_view_count)
    }

    // ViewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.guide_card, parent, false)
        return GuideViewHolder(view)
    }

    // 데이터 바인딩
    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        val guide = guideList[position]
        val title = guide.title ?: ""

        // 검색어 하이라이트 설정
        if (highlightedQuery.isNotEmpty() && title.contains(highlightedQuery, ignoreCase = true)) {
            val startIndex = title.lowercase().indexOf(highlightedQuery.lowercase())
            val endIndex = startIndex + highlightedQuery.length

            val spannable = SpannableString(title)

            spannable.setSpan(
                BackgroundColorSpan(Color.YELLOW),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                ForegroundColorSpan(Color.RED),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            holder.guideTitle.text = spannable
        } else {
            holder.guideTitle.text = title
        }

        holder.guideLocation.text = guide.locate
        holder.guideRate.text = guide.rate
        holder.guideViewCount.text= "조회수: " + guide.viewCount.toString()

        // 썸네일 이미지 설정 (imageUrls[0] 또는 기본 이미지)
        if (!guide.imageUrls.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(guide.imageUrls[0])
                .into(holder.guideImage)
        } else {
            Glide.with(holder.itemView.context)
                .load(R.drawable.image_default)
                .into(holder.guideImage)
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, GuideDetailActivity::class.java).apply {
                putExtra("guideId", guide.uId)
                putExtra("nick", guide.nick)
                putExtra("profileImageUrl", guide.profileImageUrl)
            }
            // 보내는 데이터 확인
            Log.d("GuideAdapter", "guideId: ${guide.uId}, nick: ${guide.nick}, profileImageUrl: ${guide.profileImageUrl}")
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = guideList.size

    fun highlightGuide(query: String) {
        highlightedQuery = query
        notifyDataSetChanged()
    }
}
