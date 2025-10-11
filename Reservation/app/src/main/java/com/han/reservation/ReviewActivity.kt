package com.han.reservation

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ReviewActivity : AppCompatActivity() {

    private val reviewRef = FirebaseDatabase.getInstance().getReference("reviews")
    private val reservationRef = FirebaseDatabase.getInstance().getReference("reservations")

    private lateinit var tvNoReview: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var tvReviewText: TextView
    private lateinit var ratingBarInput: RatingBar
    private lateinit var etReviewInput: EditText
    private lateinit var btnSubmitReview: Button
    private lateinit var layoutWriteReview: LinearLayout
    private lateinit var layoutReadOnlyReview: LinearLayout

    private var reservationId: String = ""
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("ReviewActivity", "onCreate START. intentReservationId=${intent.getStringExtra("reservationId")}")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)
        Log.d("ReviewActivity", "setContentView DONE")

        // 뷰 초기화
        tvNoReview = findViewById(R.id.tvNoReview)
        ratingBar = findViewById(R.id.ratingBar)
        tvReviewText = findViewById(R.id.tvReviewText)
        ratingBarInput = findViewById(R.id.ratingBarInput)
        etReviewInput = findViewById(R.id.etReviewInput)
        btnSubmitReview = findViewById(R.id.btnSubmitReview)
        layoutWriteReview = findViewById(R.id.layoutWriteReview)
        layoutReadOnlyReview = findViewById(R.id.layoutReadOnlyReview)

       // reservationId = intent.getStringExtra("reservationId") ?: return
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 현재 로그인 유저
        val auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid ?: "USER_001"

        reservationId = intent.getStringExtra("reservationId") ?: "TEST_RESERVATION"

        // 예약 상태 확인
        checkReservationStatus()
    }


    private fun checkReservationStatus() {
        Log.d("ReviewActivity", "checkReservationStatus called, reservationId=$reservationId")

        reservationRef.child(reservationId).child("status")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("ReviewActivity", "status snapshot.exists()=${snapshot.exists()} value=${snapshot.value}")
                    val status = snapshot.getValue(String::class.java) ?: ""
                    val canWriteReview = status == "completed"
                    Log.d("ReviewActivity", "canWriteReview=$canWriteReview")
                    loadReview(canWriteReview)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ReviewActivity", "checkReservationStatus cancelled: ${error.message}")
                }
            })
    }

    private fun loadReview(canWrite: Boolean) {
        Log.d("ReviewActivity", "loadReview called, canWrite=$canWrite")

        reviewRef.child(reservationId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("ReviewActivity", "review snapshot.exists()=${snapshot.exists()}")

                    if (!snapshot.exists()) {
                        if (canWrite) {
                            Log.d("ReviewActivity", "No review, user CAN write")
                            showWriteReviewUI()
                        } else {
                            Log.d("ReviewActivity", "No review, read-only mode")
                            showEmptyReadonlyUI()
                        }
                        return
                    }

                    val review = snapshot.getValue(Review::class.java)
                    Log.d("ReviewActivity", "Loaded review=$review")
                    review?.let { showReadReviewUI(it, canWrite) }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ReviewActivity", "loadReview cancelled: ${error.message}")
                }
            })
    }

    private fun showEmptyReadonlyUI() {
        Log.d("ReviewActivity", "showEmptyReadonlyUI()")

        tvNoReview.visibility = View.VISIBLE
        tvNoReview.text = "아직 후기가 없습니다."

        layoutReadOnlyReview.visibility = View.GONE
        layoutWriteReview.visibility = View.GONE
    }


    private fun showWriteReviewUI(existingReview: Review? = null) {
        Log.d("ReviewActivity", "showWriteReviewUI() called")

        tvNoReview.visibility = View.GONE
        layoutReadOnlyReview.visibility = View.GONE

        layoutWriteReview.visibility = View.VISIBLE
        btnSubmitReview.visibility = View.VISIBLE
        ratingBarInput.visibility = View.VISIBLE
        etReviewInput.visibility = View.VISIBLE

        try {
            ratingBarInput.setIsIndicator(false) // ← 안전한 호출
        } catch (e: Throwable) {
            ratingBarInput.isClickable = true
            ratingBarInput.isFocusable = true
        }

        ratingBarInput.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) {
                Log.d("ReviewActivity", "rating changed by user: $rating")
            }
        }

        // 기존 리뷰 수정일 경우
        if (existingReview != null) {
            ratingBarInput.rating = existingReview.rating.toFloat()
            etReviewInput.setText(existingReview.text)
            btnSubmitReview.text = "후기 수정"
        } else {
            ratingBarInput.rating = 0f
            etReviewInput.text.clear()
            btnSubmitReview.text = "후기 등록"
        }

        btnSubmitReview.setOnClickListener {
            val rating = ratingBarInput.rating.toInt()
            val text = etReviewInput.text.toString()

            if (rating == 0 || text.isBlank()) {
                Toast.makeText(this, "별점과 후기를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newReview = Review(rating, text, currentUserId, System.currentTimeMillis())
            reviewRef.child(reservationId).setValue(newReview)
                .addOnSuccessListener {
                    Toast.makeText(this, "후기가 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    showReadReviewUI(newReview, canWrite = true) //  읽기 모드 전환
                }
                .addOnFailureListener {
                    Toast.makeText(this, "등록 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showReadReviewUI(review: Review, canWrite: Boolean) {
        Log.d("ReviewActivity", "showReadReviewUI() called")

        tvNoReview.visibility = View.GONE
        layoutReadOnlyReview.visibility = View.VISIBLE
        layoutWriteReview.visibility = View.GONE

        ratingBar.rating = review.rating.toFloat()
        tvReviewText.text = review.text


        // 수정 (작성자만)
        if (canWrite && review.userId == currentUserId) {
            tvReviewText.setOnClickListener {
                Toast.makeText(this, "수정 모드로 전환합니다.", Toast.LENGTH_SHORT).show()
                showWriteReviewUI(review) // 수정 모드로 전환
            }
            tvReviewText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit, 0)
            tvReviewText.compoundDrawablePadding = 8
        } else {
            tvReviewText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
