package com.han.kkatalk2

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
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
    private var searchQuery: String = ""
) : RecyclerView.Adapter<GuideAdapter.GuideViewHolder>() {

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

        holder.guideTitle.text = guide.title
        holder.guideLocation.text = guide.locate
        holder.guideRate.text = guide.rate
        holder.guideViewCount.text= "조회수: " + guide.viewCount.toString()


        // 썸네일 이미지 설정 (imageUrls[0] 또는 기본 이미지)ㅅ
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

    // 검색어 하이라이트 처리
    private fun getHighlightedText(fullText: String, keyword: String): SpannableString {
        val spannable = SpannableString(fullText)
        if (keyword.isEmpty()) return spannable

        val lowerFull = fullText.lowercase()
        val lowerKeyword = keyword.lowercase()

        var startIndex = lowerFull.indexOf(lowerKeyword)
        while (startIndex >= 0) {
            val endIndex = startIndex + keyword.length
            spannable.setSpan(
                ForegroundColorSpan(Color.RED), // 색상
                startIndex, endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                StyleSpan(Typeface.BOLD), // 굵게
                startIndex, endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            startIndex = lowerFull.indexOf(lowerKeyword, endIndex)
        }
        return spannable
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
        notifyDataSetChanged()
    }
}
