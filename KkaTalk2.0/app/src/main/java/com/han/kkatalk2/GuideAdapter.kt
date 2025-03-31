package com.han.kkatalk2

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class GuideAdapter(private val guideList: List<Guide>) : RecyclerView.Adapter<GuideAdapter.GuideViewHolder>() {

    // ViewHolder 클래스 정의
    class GuideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val guideImage: ImageView = itemView.findViewById(R.id.guideImage)
        val guideName: TextView = itemView.findViewById(R.id.guideName)
        val guideLocation: TextView = itemView.findViewById(R.id.guideLocation)
        val guideRate: TextView = itemView.findViewById(R.id.guideRate)
    }

    // ViewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.guide_card, parent, false)
        return GuideViewHolder(view)
    }

    // 데이터 바인딩
    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        val guide = guideList[position]

        holder.guideName.text = guide.name
        holder.guideLocation.text = guide.locate
        holder.guideRate.text = guide.rate


        // Glide 사용하여 프로필 이미지 로드
        Glide.with(holder.itemView.context)
            .load(guide.profileImageUrl)
            .into(holder.guideImage)

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
}
