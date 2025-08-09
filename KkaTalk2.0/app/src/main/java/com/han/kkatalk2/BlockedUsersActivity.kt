package com.han.kkatalk2

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BlockedUsersActivity : AppCompatActivity() {

    private lateinit var userAdapter: UserAdapter
    private val blockedUsers = ArrayList<User>()
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var prefs: SharedPreferences
    private lateinit var userRef: DatabaseReference
    private lateinit var currentUserId: String

    private val TAG = "BlockedUsersActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked_users)

        // 초기화
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        currentUserId = auth.currentUser?.uid
            ?: prefs.getString("userId", null).orEmpty()

        if (currentUserId.isEmpty()) {
            Log.e(TAG, "현재 사용자 ID를 찾을 수 없습니다.")
            finish()
            return
        }

        userRef = database.reference.child("user")

        // RecyclerView 설정
        val recyclerView: RecyclerView = findViewById(R.id.rvBlockedUsers)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(this, blockedUsers)
        recyclerView.adapter = userAdapter

        fetchBlockedUsers()

        userAdapter.setOnItemClickListener { user ->
            showUnblockDialog(user)
        }

        // 툴바 뒤로가기
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // 차단된 사용자 ID 목록을 기반으로 유저 정보 조회
    private fun fetchBlockedUsers() {
        val blockedRef = database.reference.child("user").child(currentUserId).child("blockedUsers")

        blockedRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                blockedUsers.clear()
                val blockedIds = snapshot.children.mapNotNull { it.key }

                if (blockedIds.isEmpty()) {
                    userAdapter.updateList(blockedUsers)
                    return
                }

                var loadedCount = 0

                for (uid in blockedIds) {
                    userRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnap: DataSnapshot) {
                            val user = userSnap.getValue(User::class.java)
                            if (user != null) {
                                blockedUsers.add(user)
                            }
                            loadedCount++
                            if (loadedCount == blockedIds.size) {
                                userAdapter.updateList(blockedUsers)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "유저 정보 조회 실패: ${error.message}")
                            loadedCount++
                            if (loadedCount == blockedIds.size) {
                                userAdapter.updateList(blockedUsers)
                            }
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "차단 목록 조회 실패: ${error.message}")
            }
        })
    }

    private fun showUnblockDialog(user: User) {
        MaterialAlertDialogBuilder(this)
            .setTitle("차단 해제")
            .setMessage("${user.nick} 님의 차단을 해제하시겠습니까?")
            .setPositiveButton("해제") { _, _ -> unblockUser(user) }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun unblockUser(user: User) {
        val blockedRef = database.reference.child("user").child(currentUserId).child("blockedUsers")

        blockedRef.child(user.uId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "${user.nick} 차단 해제 성공")
                blockedUsers.remove(user)
                userAdapter.updateList(blockedUsers)
            } else {
                Log.e(TAG, "차단 해제 실패: ${task.exception?.message}")
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
