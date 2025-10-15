package com.han.reservation

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ReviewActivity : AppCompatActivity() {

    private val TAG = "ReviewActivity"

    private val reviewRef = FirebaseDatabase.getInstance().getReference("reviews")
    private val reservationRef = FirebaseDatabase.getInstance().getReference("reservations")

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvNoReview: TextView
    private lateinit var layoutWriteReview: LinearLayout
    private lateinit var ratingBarInput: RatingBar
    private lateinit var etReviewInput: EditText
    private lateinit var btnSubmitReview: Button

    private lateinit var adapter: ReviewAdapter

    private var reservationId: String = ""
    private var currentUserId: String = ""
    private var canWriteReview: Boolean = false

    private var reviewsListener: ValueEventListener? = null
    private var reviewsRefForThisReservation: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("ReviewActivity", "onCreate START. intentReservationId=${intent.getStringExtra("reservationId")}")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)
        Log.d("ReviewActivity", "setContentView DONE")

        // 뷰 초기화
        recyclerView = findViewById(R.id.recyclerReviews)
        tvNoReview = findViewById(R.id.tvNoReview)
        layoutWriteReview = findViewById(R.id.layoutWriteReview)
        ratingBarInput = findViewById(R.id.ratingBarInput)
        etReviewInput = findViewById(R.id.etReviewInput)
        btnSubmitReview = findViewById(R.id.btnSubmitReview)

        // reservationId = intent.getStringExtra("reservationId") ?: return
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 현재 로그인 유저
        val auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid ?: "USER_001"

        reservationId = intent.getStringExtra("reservationId") ?: "TEST_RESERVATION"

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ReviewAdapter(
            currentUserId,
            onEdit = { review -> showEditMode(review) },
            onDelete = { review -> confirmDelete(review) }
        )
        recyclerView.adapter = adapter

        btnSubmitReview.setOnClickListener {
            val rating = ratingBarInput.rating.toInt()
            val text = etReviewInput.text.toString().trim()

            if (rating == 0 || text.isBlank()) {
                Toast.makeText(this, "별점과 후기를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // write: userId node under reservationId (allows multiple users to leave reviews for same reservation)
            val newReview = Review(rating, text, currentUserId, System.currentTimeMillis())
            reviewRef.child(reservationId).child(currentUserId).setValue(newReview)
                .addOnSuccessListener {
                    Log.d(TAG, "Review written success")
                    Toast.makeText(this, "후기가 등록되었습니다.", Toast.LENGTH_SHORT).show()

                    // UI 정리: 키보드 숨기고 입력값 초기화, 폼 숨김
                    hideKeyboard()
                    etReviewInput.text.clear()
                    ratingBarInput.rating = 0f
                    // 폼 숨기기 (실시간 리스너가 목록을 갱신해 줄 것)
                    layoutWriteReview.visibility = View.GONE

                    // 스크롤을 갱신(짧은 지연 후) — 실시간 반영을 기다린 뒤 스크롤
                    recyclerView.postDelayed({
                        adapter.notifyDataSetChanged()
                        if (adapter.itemCount > 0) recyclerView.smoothScrollToPosition(0)
                    }, 200)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Review write failed", e)
                    Toast.makeText(this, "등록 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // 예약 상태 확인
        checkReservationStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 리스너 해제
        reviewsListener?.let { reviewsRefForThisReservation?.removeEventListener(it) }
    }

    private fun checkReservationStatus() {
        Log.d(TAG, "checkReservationStatus called, reservationId=$reservationId")

        reservationRef.child(reservationId).child("status")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "status snapshot.exists()=${snapshot.exists()} value=${snapshot.value}")
                    val status = snapshot.getValue(String::class.java) ?: ""
                    canWriteReview = status == "completed" || status == "CONFIRMED" || status == "confirmed"
                    // 위 line: 환경에 따라 상태 문자열이 달라질 수 있어 안전하게 둘 다 체크
                    Log.d(TAG, "canWriteReview=$canWriteReview")
                    // 이제 리뷰 실시간 로드 시작
                    startReviewListener()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "checkReservationStatus cancelled: ${error.message}")
                }
            })
    }

    private fun startReviewListener() {
        // 기존 리스너 있으면 제거
        reviewsListener?.let { reviewsRefForThisReservation?.removeEventListener(it) }

        reviewsRefForThisReservation = reviewRef.child(reservationId)
        reviewsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "reviews onDataChange, children=${snapshot.childrenCount}")

                val list = mutableListOf<Review>()
                for (child in snapshot.children) {
                    val review = child.getValue(Review::class.java)
                    if (review != null) list.add(review)
                }

                // UI 업데이트: 리뷰가 있으면 목록, 없으면 작성/없음 표시
                if (list.isNotEmpty()) {
                    recyclerView.visibility = View.VISIBLE
                    tvNoReview.visibility = View.GONE
                    // 작성 폼은 작성 가능 여부에 따라 노출
                    layoutWriteReview.visibility = if (canWriteReview) View.VISIBLE else View.GONE

                    // 제출 버튼 텍스트 초기화
                    btnSubmitReview.text = "후기 등록"

                    // 어댑터 갱신
                    adapter.submitList(list)
                } else {
                    // 리뷰가 없는 경우
                    recyclerView.visibility = View.GONE
                    if (canWriteReview) {
                        tvNoReview.visibility = View.GONE
                        layoutWriteReview.visibility = View.VISIBLE
                    } else {
                        tvNoReview.visibility = View.VISIBLE
                        tvNoReview.text = "아직 후기가 없습니다."
                        layoutWriteReview.visibility = View.GONE
                    }
                    adapter.submitList(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "loadReviews cancelled: ${error.message}")
            }
        }

        // addValueEventListener 등록 (실시간)
        reviewsRefForThisReservation?.addValueEventListener(reviewsListener as ValueEventListener)
    }

    private fun showEditMode(review: Review) {
        Log.d(TAG, "showEditMode for ${review.userId}")
        layoutWriteReview.visibility = View.VISIBLE
        ratingBarInput.rating = review.rating.toFloat()
        etReviewInput.setText(review.text)
        btnSubmitReview.text = "후기 수정"

        // 수정 버튼 동작 — 기존 submit 리스너와 중복되지 않게 임시 리스너 사용
        btnSubmitReview.setOnClickListener {
            val updatedRating = ratingBarInput.rating.toInt()
            val updatedText = etReviewInput.text.toString().trim()

            if (updatedRating == 0 || updatedText.isBlank()) {
                Toast.makeText(this, "별점과 후기를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedReview = review.copy(
                rating = updatedRating,
                text = updatedText,
                createdAt = System.currentTimeMillis()
            )

            // 덮어쓰기: currentUserId 하위 노드
            reviewRef.child(reservationId).child(currentUserId).setValue(updatedReview)
                .addOnSuccessListener {
                    Toast.makeText(this, "후기가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                    hideKeyboard()
                    etReviewInput.text.clear()
                    ratingBarInput.rating = 0f
                    btnSubmitReview.text = "후기 등록"
                    // 원래의 submit 리스너로 되돌리기:
                    resetSubmitListenerToCreateMode()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "수정 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun resetSubmitListenerToCreateMode() {
        // 원래의 "등록" 동작으로 되돌림
        btnSubmitReview.setOnClickListener {
            val rating = ratingBarInput.rating.toInt()
            val text = etReviewInput.text.toString().trim()

            if (rating == 0 || text.isBlank()) {
                Toast.makeText(this, "별점과 후기를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newReview = Review(rating, text, currentUserId, System.currentTimeMillis())
            reviewRef.child(reservationId).child(currentUserId).setValue(newReview)
                .addOnSuccessListener {
                    Toast.makeText(this, "후기가 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    hideKeyboard()
                    etReviewInput.text.clear()
                    ratingBarInput.rating = 0f
                    layoutWriteReview.visibility = View.GONE
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "등록 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun confirmDelete(review: Review) {
        AlertDialog.Builder(this)
            .setTitle("후기 삭제")
            .setMessage("정말로 이 후기를 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                reviewRef.child(reservationId).child(review.userId).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "후기가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "삭제 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun hideKeyboard() {
        try {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            var v = currentFocus
            if (v == null) v = View(this)
            imm.hideSoftInputFromWindow(v.windowToken, 0)
            v.clearFocus()
        } catch (e: Exception) {
            Log.w(TAG, "hideKeyboard failed: ${e.message}")
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
