package com.han.kkatalk2

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var prefs: SharedPreferences
    private lateinit var userRef: DatabaseReference
    private var userId: String = ""

    private val TAG = "BlockUsersActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked_users)

        // Firebase 설정
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        blockedUserIds = ArrayList()
        allUsers = ArrayList()

        // 유저 정보 불러오기
        val currentUser = auth.currentUser
        if (currentUser != null) {
            userId = currentUser.uid
            Log.d(TAG,"userid: $userId")
        } else {
            // SharedPreferences에서 userid 가져오기
            userId = prefs.getString("userId", null) ?: ""
            Log.d(TAG,"userid: $userId")
        }

        userRef = database.getReference("user").child(userId)

        // RecyclerView 설정
        val recyclerView: RecyclerView = findViewById(R.id.rvBlockedUsers)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(this, ArrayList())
        recyclerView.adapter = userAdapter

        fetchBlockedUsers()

        userAdapter.setOnItemClickListener { user ->
            showUnblockDialog(user)
        }

        // 툴바에 뒤로가기 버튼 추가
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
    }

    // 전체 유저 목록
    private fun fetchAllUsers() {
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

    // 차단 유저 목록
    private fun fetchBlockedUsers() {
        if (userId.isNotEmpty()) {
            userRef.child("blockedUsers").addValueEventListener(object : ValueEventListener {
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

    // 차단 해제 다이얼로그
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

    // 차단 해제 기능
    private fun unblockUser(user: User) {
        val blockedUsersRef = database.reference.child("user").child(userId).child("blockedUsers")

        blockedUsersRef.child(user.uId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("BlockedUsersActivity", "${user.nick} 차단 해제 성공")
            } else {
                Log.e("BlockedUsersActivity", "${user.nick} 차단 해제 실패: ${task.exception?.message}")
            }
        }
    }
    
    // 뒤로 가기 버튼
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { // 뒤로가기 버튼 클릭 이벤트 처리
                Log.d("BlockedUsersActivity", "뒤로가기 버튼 클릭됨")
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}