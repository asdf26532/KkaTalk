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

       // reservationId = intent.getStringExtra("reservationId") ?: return
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 현재 로그인 유저
        val auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid ?: "USER_001"

        reservationId = intent.getStringExtra("reservationId") ?: "TEST_RESERVATION"


        // 테스트용 데이터 삽입
        insertTestDataIfNeeded()

        // 예약 상태 확인
        checkReservationStatus()
    }

    private fun insertTestDataIfNeeded() {
        reservationRef.child("TEST_RESERVATION")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        val testReservation = mapOf(
                            "userId" to "USER_001",
                            "guideId" to "GUIDE_001",
                            "status" to "completed", // 후기 작성 가능 상태
                            "title" to "테스트 예약"
                        )
                        reservationRef.child("TEST_RESERVATION").setValue(testReservation)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
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
        reviewRef.child(reservationId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        if (canWrite) showWriteReviewUI()
                        else showEmptyReadonlyUI()
                        return
                    }

                    val review = snapshot.getValue(Review::class.java)
                    review?.let { showReadReviewUI(it, canWrite) }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun showEmptyReadonlyUI() {
        tvNoReview.visibility = View.VISIBLE
        tvNoReview.text = "아직 후기가 없습니다."

        ratingBar.visibility = View.GONE
        tvReviewText.visibility = View.GONE
        ratingBarInput.visibility = View.GONE
        etReviewInput.visibility = View.GONE
        btnSubmitReview.visibility = View.GONE
    }

    private fun showWriteReviewUI() {
        tvNoReview.visibility = View.GONE
        ratingBar.visibility = View.GONE
        tvReviewText.visibility = View.GONE

        ratingBarInput.visibility = View.VISIBLE
        etReviewInput.visibility = View.VISIBLE
        btnSubmitReview.visibility = View.VISIBLE

        btnSubmitReview.setOnClickListener {
            val rating = ratingBarInput.rating.toInt()
            val text = etReviewInput.text.toString()

            if (rating == 0 || text.isBlank()) {
                Toast.makeText(this, "별점과 후기를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val review = Review(rating, text, currentUserId, System.currentTimeMillis())
            reviewRef.child(reservationId).setValue(review)
                .addOnSuccessListener {
                    Toast.makeText(this, "후기가 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    showReadReviewUI(review, canWrite = false)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "등록 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showReadReviewUI(review: Review, canWrite: Boolean) {
        tvNoReview.visibility = View.GONE

        ratingBar.visibility = View.VISIBLE
        tvReviewText.visibility = View.VISIBLE

        ratingBarInput.visibility = View.GONE
        etReviewInput.visibility = View.GONE
        btnSubmitReview.visibility = View.GONE

        ratingBar.rating = review.rating.toFloat()
        tvReviewText.text = review.text

        // 만약 후기를 수정 가능하게 할 경우 (본인 후기)
        if (canWrite && review.userId == currentUserId) {
            btnSubmitReview.visibility = View.VISIBLE
            btnSubmitReview.text = "수정하기"
            btnSubmitReview.setOnClickListener {
                ratingBarInput.visibility = View.VISIBLE
                etReviewInput.visibility = View.VISIBLE
                ratingBarInput.rating = review.rating.toFloat()
                etReviewInput.setText(review.text)
                btnSubmitReview.text = "등록"
                showWriteReviewUI()
            }
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
