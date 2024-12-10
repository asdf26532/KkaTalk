package com.han.kkaTalk

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RandomChatSetupActivity : AppCompatActivity() {

    private lateinit var mDbRef: DatabaseReference
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_random_chat_setup)


        mDbRef = FirebaseDatabase.getInstance().reference
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val etNickname = findViewById<EditText>(R.id.etNickname)
        val cbMusic = findViewById<CheckBox>(R.id.cbMusic)
        val cbSports = findViewById<CheckBox>(R.id.cbSports)
        val cbMovies = findViewById<CheckBox>(R.id.cbMovies)
        val btnStartRandomChat = findViewById<Button>(R.id.btnStartRandomChat)

        btnStartRandomChat.setOnClickListener {
            val nickname = etNickname.text.toString()
            val interests = mutableListOf<String>()
            if (cbMusic.isChecked) interests.add("music")
            if (cbSports.isChecked) interests.add("sports")
            if (cbMovies.isChecked) interests.add("movies")

            if (nickname.isEmpty() || interests.isEmpty()) {
                Toast.makeText(this, "Fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Add user to random queue
            mDbRef.child("randomQueue").child(userId).setValue(
                mapOf(
                    "nickname" to nickname,
                    "interests" to interests,
                    "timestamp" to System.currentTimeMillis()
                )
            )
           // startActivity(Intent(this, RandomChatActivity::class.java)) // 채팅 대기 액티비티로 이동
        }
    }
}