package com.han.kkaTalk

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.han.kkaTalk.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: UserAdapter
    private lateinit var userList: ArrayList<User>
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    private var refreshRequired = false

    private val blockedUserIds = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        mAuth = FirebaseAuth.getInstance()
        mDbRef = Firebase.database.reference

        // 사용자 리스트 초기화
        userList = ArrayList()
        adapter = UserAdapter(requireContext(), userList)

        binding.rvUser.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUser.adapter = adapter


        // 차단된 사용자 데이터 먼저 가져오기
        fetchBlockedUsers {
            // 차단된 사용자 데이터를 가져온 후 사용자 목록 가져오기
            fetchUserData()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        // 새로고침 요청이 있을 경우 차단 목록 다시 로드
        if (refreshRequired) {
            refreshUserList()
            refreshRequired = false // 초기화
        }
    }

    private fun refreshUserList() {
        fetchBlockedUsers {
            fetchUserData() // 사용자 목록 새로고침
        }
    }

    // 차단 유저 목록
    private fun fetchBlockedUsers(callback: () -> Unit) {
        val currentUserId = mAuth.currentUser?.uid ?: return
        val blockedUsersRef = mDbRef.child("user").child(currentUserId).child("blockedUsers")

        blockedUsersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                blockedUserIds.clear()
                for (blockedUserSnapshot in snapshot.children) {
                    val blockedUserId = blockedUserSnapshot.key.toString()
                    blockedUserIds.add(blockedUserId)
                    // 차단된 유저 UID 로그
                    Log.d("fetchBlockedUsers", "Blocked User UID: $blockedUserId")
                }
                Log.d("fetchBlockedUsers", "Total Blocked Users: $blockedUserIds")
                callback() // 차단된 사용자 데이터를 가져온 후 처리 계속
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("fetchBlockedUsers", "Error fetching blocked users: ${error.message}")
            }
        })
    }

    private fun fetchUserData() {
        // 현재 로그인한 사용자의 UID를 가져옴
        val currentUserId = mAuth.currentUser?.uid ?: return

        // Firebase에서 사용자 데이터를 가져오기
        mDbRef.child("user").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (postSnapshot in snapshot.children) {
                    val currentUser = postSnapshot.getValue(User::class.java)
                    currentUser?.let {
                        // currentUser가 null이 아니고, 현재 로그인된 사용자도 아니며, 차단된 사용자도 아닐 때 리스트에 추가
                        if (it.uId != currentUserId && !blockedUserIds.contains(it.uId)) {
                            userList.add(it)
                            Log.d("fetchUserData", "User Added: ${it.uId}, Status: ${it.statusMessage}")
                        }
                    }
                }
                // 어댑터에 데이터 변경 알림
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("fetchUserData", "Error fetching user data: ${error.message}")
            }
        })
        // 사용자 목록 아이템 클릭 시 팝업 다이얼로그 표시
        adapter.setOnItemClickListener { user ->
            showPopupDialog(user)
        }
    }

    private fun showPopupDialog(user: User) {
        val options = arrayOf("프로필 보기", "대화하기", "차단하기", "취소")

        // AlertDialog Builder 생성
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("") // 프로필사진,닉네임 표시되도록 변경
            .setItems(options) { dialog, which ->
                when (options[which]) {
                    "프로필 보기" -> {
                        val intent = Intent(activity, ProfileActivity::class.java)
                        intent.putExtra("nick", user.nick)
                        intent.putExtra("uId", user.uId)
                        intent.putExtra("profileImageUrl", user.profileImageUrl)
                        startActivity(intent)
                    }
                    "대화하기" -> {
                        val intent = Intent(requireContext(), ChatActivity::class.java)
                        intent.putExtra("nick", user.nick)
                        intent.putExtra("uId", user.uId)
                        intent.putExtra("profileImageUrl", user.profileImageUrl)
                        startActivity(intent)
                    }
                    "차단하기" -> {
                        // 차단 확인 다이얼로그 표시
                        showBlockConfirmationDialog(user)
                    }
                    "취소" -> {
                        dialog.dismiss() // 취소 로직
                    }
                }
            }

        // 다이얼로그 표시
        val dialog = builder.create()
        dialog.show()
    }

    private fun showBlockConfirmationDialog(user: User) {
        // 차단 확인 다이얼로그 표시
        AlertDialog.Builder(requireContext())
            .setTitle("차단하기")
            .setMessage("${user.nick} 님을 차단하시겠습니까?")
            .setPositiveButton("차단") { _, _ ->
                blockUser(user) // 차단 처리 로직 호출
            }
            .setNegativeButton("취소", null) // 아무 동작 없이 닫기
            .show()
    }

    // 유저 차단
    private fun blockUser(user: User) {
        val currentUserId = mAuth.currentUser?.uid ?: return
        val blockedUsersRef = mDbRef.child("user").child(currentUserId).child("blockedUsers")

        blockedUsersRef.child(user.uId).setValue(true).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "${user.nick} 님을 차단했습니다.", Toast.LENGTH_SHORT).show()
                Log.d("HomeFragment", "User ${user.uId} blocked successfully.")
                // 데이터 새로고침
                fetchBlockedUsers {
                    fetchUserData() // 차단된 사용자 제외하고 리스트 갱신
                }

            } else {
                Toast.makeText(requireContext(), "차단에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                Log.e("HomeFragment", "Failed to block user: ${task.exception?.message}")
            }
        }
    }

}
