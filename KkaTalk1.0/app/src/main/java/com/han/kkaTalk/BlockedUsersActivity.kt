package com.han.kkaTalk

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

        fetchBlockedUsers()

        userAdapter.setOnItemClickListener { user ->
            showUnblockDialog(user)
        }
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

                    fetchAllUsers()
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

    private fun showUnblockDialog(user: User) {
        MaterialAlertDialogBuilder(this)
            .setTitle("차단 해제")
            .setMessage("차단을 해제하시겠습니까?")
            .setPositiveButton("해제") { _, _ ->
                unblockUser(user)
            }
            .setNegativeButton("취소", null) // 아무 동작 없이 닫기
            .show()
    }

    private fun unblockUser(user: User) {
        val currentUserId = mAuth.currentUser?.uid ?: return
        val blockedUsersRef = mDbRef.child("user").child(currentUserId).child("blockedUsers")

        blockedUsersRef.child(user.uId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("BlockedUsersActivity", "${user.nick} 차단 해제 성공")
            } else {
                Log.e("BlockedUsersActivity", "${user.nick} 차단 해제 실패: ${task.exception?.message}")
            }
        }
    }




}