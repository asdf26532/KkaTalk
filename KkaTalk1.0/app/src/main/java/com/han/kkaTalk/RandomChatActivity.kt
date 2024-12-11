package com.han.kkaTalk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RandomChatActivity : AppCompatActivity() {

    private lateinit var mDbRef: DatabaseReference
    private lateinit var userId: String

    private lateinit var tvStatus: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_random_chat)

        // Firebase 설정
        mDbRef = FirebaseDatabase.getInstance().reference
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // UI 연결
        tvStatus = findViewById(R.id.tvStatus)
        progressBar = findViewById(R.id.progressBar)
        btnCancel = findViewById(R.id.btnCancel)

        // 매칭 취소 버튼
        btnCancel.setOnClickListener {
            cancelMatching()
        }

        // 매칭 시작
        tvStatus.text = "Finding someone to chat with..."
        findMatch()
    }

    private fun findMatch() {
        val queueRef = mDbRef.child("randomQueue")
        queueRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var matchedUserId: String? = null
                var matchedNickname: String? = null

                for (child in snapshot.children) {
                    val uid = child.key ?: continue
                    if (uid == userId) continue // 본인은 제외

                    matchedUserId = uid
                    matchedNickname = child.child("nickname").value as? String
                    break
                }

                if (matchedUserId != null) {
                    val roomId = "room_${userId}_${matchedUserId}"
                    createChatRoom(roomId, matchedUserId, matchedNickname)
                } else {
                    tvStatus.text = "No match found. Try again later."
                    progressBar.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                tvStatus.text = "Error finding match. Please try again."
                progressBar.visibility = View.GONE
            }
        })
    }

    private fun createChatRoom(roomId: String, matchedUserId: String, matchedNickname: String?) {
        mDbRef.child("randomChats").child(roomId).setValue(
            mapOf(
                "user1" to userId,
                "user2" to matchedUserId,
                "messages" to emptyList<Message>()
            )
        )
        tvStatus.text = "Matched with $matchedNickname!"
        progressBar.visibility = View.GONE

        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("roomId", roomId)
        startActivity(intent)
        finish()
    }

    private fun cancelMatching() {
        mDbRef.child("randomQueue").child(userId).removeValue().addOnSuccessListener {
            Toast.makeText(this, "Matching canceled", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to cancel matching. Try again.", Toast.LENGTH_SHORT).show()
        }
    }


}