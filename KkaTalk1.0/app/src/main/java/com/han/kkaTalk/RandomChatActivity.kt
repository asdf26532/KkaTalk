package com.han.kkaTalk

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_random_chat)

        mDbRef = FirebaseDatabase.getInstance().reference
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        findMatch()
    }

    private fun findMatch() {
        val queueRef = mDbRef.child("randomQueue")
        queueRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var matchedUserId: String? = null
                var matchedNickname: String? = null
                var matchedInterests: List<String>? = null

                for (child in snapshot.children) {
                    val uid = child.key ?: continue
                    if (uid == userId) continue // 본인은 제외

                    val interests = child.child("interests").value as? List<String>
                    if (interests != null) {
                        matchedUserId = uid
                        matchedNickname = child.child("nickname").value as? String
                        matchedInterests = interests
                        break
                    }
                }

                if (matchedUserId != null) {
                    val roomId = "room_${userId}_${matchedUserId}"
                    createChatRoom(roomId, matchedUserId, matchedNickname)
                } else {
                    Toast.makeText(this@RandomChatActivity, "No match found. Try again later.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RandomChat", "Error finding match: ${error.message}")
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
        Toast.makeText(this, "Matched with $matchedNickname!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("roomId", roomId)
        startActivity(intent)
    }


}