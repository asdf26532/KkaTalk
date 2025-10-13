package com.han.reservation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReviewAdapter(
    private val currentUserId: String,
    private val onEdit: (Review) -> Unit,
    private val onDelete: (Review) -> Unit
) : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    private val reviewList = mutableListOf<Review>()

    fun submitList(list: List<Review>) {
        reviewList.clear()
        reviewList.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvReviewerName: TextView = view.findViewById(R.id.tvReviewerName)
        val ratingBarItem: RatingBar = view.findViewById(R.id.ratingBarItem)
        val tvReviewTextItem: TextView = view.findViewById(R.id.tvReviewTextItem)
        val layoutReviewActions: LinearLayout = view.findViewById(R.id.layoutReviewActions)
        val btnEdit: ImageView = view.findViewById(R.id.btnEditReview)
        val btnDelete: ImageView = view.findViewById(R.id.btnDeleteReview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = reviewList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = reviewList[position]
        holder.tvReviewerName.text = review.userId
        holder.ratingBarItem.rating = review.rating.toFloat()
        holder.tvReviewTextItem.text = review.text

        if (review.userId == currentUserId) {
            holder.layoutReviewActions.visibility = View.VISIBLE
            holder.btnEdit.setOnClickListener { onEdit(review) }
            holder.btnDelete.setOnClickListener { onDelete(review) }
        } else {
            holder.layoutReviewActions.visibility = View.GONE
        }
    }
}