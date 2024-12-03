package com.han.kkaTalk

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BlockedUsersActivity : AppCompatActivity() {

    private lateinit var userAdapter: UserAdapter
    private lateinit var blockedUserIds: ArrayList<String>
    private lateinit var allUsers: ArrayList<User>
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked_users)

        // Firebase 설정
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference
        blockedUserIds = ArrayList()
        allUsers = ArrayList()

        // RecyclerView 설정
        val recyclerView: RecyclerView = findViewById(R.id.rvBlockedUsers)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(this, ArrayList())
        recyclerView.adapter = userAdapter

        fetchAllUsers()
        fetchBlockedUsers()
    }

    private fun fetchAllUsers() {
        val userRef = mDbRef.child("user")
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allUsers.clear()
                for (child in snapshot.children) {
                    val user = child.getValue(User::class.java)
                    if (user != null) {
                        allUsers.add(user)
                    }
                }
                updateAdapter() // 차단된 사용자 목록 갱신
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("BlockedUsersActivity", "Failed to fetch all users: ${error.message}")
            }
        })
    }

    private fun fetchBlockedUsers() {
        val currentUserId = mAuth.currentUser?.uid
        if (currentUserId != null) {
            val userRef = mDbRef.child("user").child(currentUserId).child("blockedUsers")

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    blockedUserIds.clear()
                    for (child in snapshot.children) {
                        val blockedUserId = child.key
                        if (blockedUserId != null) {
                            blockedUserIds.add(blockedUserId)
                        }
                    }
                    updateAdapter() // 차단된 사용자 목록 갱신
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("BlockedUsersActivity", "Failed to fetch blocked users: ${error.message}")
                }
            })
        }
    }

    private fun updateAdapter() {
        userAdapter.updateList(blockedUserIds, allUsers)
    }
}