package com.han.kkatalk2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.han.kkatalk2.databinding.ActivityUserManagementBinding

class UserManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserManagementBinding
    private lateinit var adapter: UserAdapter
    private lateinit var userList: ArrayList<User>
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        mDbRef = Firebase.database.reference

        userList = ArrayList()
        adapter = UserAdapter(this, userList)
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter

        adapter.setOnItemClickListener { user ->
            showUserOptions(user)
        }

        fetchAllUsers()

        // 툴바에 뒤로가기 버튼 추가
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun fetchAllUsers() {
        mDbRef.child("user").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                val currentUid = mAuth.currentUser?.uid
                for (userSnap in snapshot.children) {
                    val user = userSnap.getValue(User::class.java)
                    user?.let {
                        if (it.uId != currentUid) { // 본인은 목록에서 제외
                            userList.add(it)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                showCustomToast("사용자 목록 로드 실패")
            }
        })
    }

    private fun showUserOptions(user: User) {
        val options = arrayOf("프로필 보기", "대화하기", "신고 처리", "정지 해제", "취소")

        AlertDialog.Builder(this)
            .setTitle("${user.nick}님 선택")
            .setItems(options) { dialog, which ->
                when (options[which]) {
                    "프로필 보기" -> {
                        val intent = Intent(this, ProfileActivity::class.java)
                        intent.putExtra("uId", user.uId)
                        intent.putExtra("nick", user.nick)
                        intent.putExtra("profileImageUrl", user.profileImageUrl)
                        startActivity(intent)
                    }

                    "대화하기" -> {
                        val intent = Intent(this, ChatActivity::class.java)
                        intent.putExtra("uId", user.uId)
                        intent.putExtra("nick", user.nick)
                        intent.putExtra("profileImageUrl", user.profileImageUrl)
                        startActivity(intent)
                    }

                    "신고 처리" -> {
                        val intent = Intent(this, ReportManagementActivity::class.java)
                        intent.putExtra("userId", user.uId)
                        startActivity(intent)
                    }

                    "정지 해제" -> {
                        unbanUser(user.uId)
                    }

                    else -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun unbanUser(uid: String) {
        val userRef = Firebase.database.reference.child("user").child(uid)

        userRef.child("banUntil").setValue(0L)
            .addOnSuccessListener {
                showCustomToast("계정 정지가 해제되었습니다.")
            }
            .addOnFailureListener {
                showCustomToast("정지 해제 실패: ${it.message}")
            }
    }

    // 뒤로 가기 버튼
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { // 뒤로가기 버튼 클릭 이벤트 처리
                Log.d("ProfileActivity", "뒤로가기 버튼 클릭됨")
                onBackPressedDispatcher.onBackPressed()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}