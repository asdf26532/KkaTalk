package com.han.reservation

import android.os.Bundle
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class ReviewActivity : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance().getReference("reviews")
    private lateinit var tvNoReview: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var tvReviewText: TextView

    private var requestId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        tvNoReview = findViewById(R.id.tvNoReview)
        ratingBar = findViewById(R.id.ratingBar)
        tvReviewText = findViewById(R.id.tvReviewText)

        requestId = intent.getStringExtra("requestId") ?: return

        loadReview()
    }

    private fun loadReview() {
        database.child(requestId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    tvNoReview.text = "아직 후기가 없습니다."
                    tvNoReview.visibility = android.view.View.VISIBLE
                    ratingBar.visibility = android.view.View.GONE
                    tvReviewText.visibility = android.view.View.GONE
                    return
                }

                val r = snapshot.getValue(Review::class.java)
                r?.let {
                    tvNoReview.visibility = android.view.View.GONE
                    ratingBar.visibility = android.view.View.VISIBLE
                    tvReviewText.visibility = android.view.View.VISIBLE

                    ratingBar.rating = it.rating.toFloat()
                    tvReviewText.text = it.text
                } ?: run {
                    Toast.makeText(this@ReviewViewActivity, "후기 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}