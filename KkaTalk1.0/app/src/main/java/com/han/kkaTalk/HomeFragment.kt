package com.han.kkaTalk

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
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
        val currentUserId = mAuth.currentUser?.uid ?: return

        // Firebase에서 모든 사용자 데이터를 가져옵니다.
        mDbRef.child("user").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear() // 사용자 리스트 초기화
                for (postSnapshot in snapshot.children) {
                    val currentUser = postSnapshot.getValue(User::class.java)

                    if (currentUser != null) {
                        if (currentUser.uId != currentUserId) {
                            if (blockedUserIds.contains(currentUser.uId)) {
                                // 차단된 유저가 필터링되는지 확인
                                Log.d("fetchUserData", "Blocked User Skipped: ${currentUser.uId}")
                            } else {
                                userList.add(currentUser)
                                // 차단되지 않은 유저 로그
                                Log.d("fetchUserData", "User Added: ${currentUser.uId}")
                            }
                        }
                    }
                }
                Log.d("fetchUserData", "Final User List: ${userList.map { it.uId }}")
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("fetchUserData", "Error fetching user data: ${error.message}")
            }
        })

        adapter.setOnItemClickListener { user ->
            showPopupDialog(user)
        }
    }

    private fun showPopupDialog(user: User) {
        val options = arrayOf("프로필 보기", "대화하기", "취소")

        // AlertDialog Builder 생성
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("") // 프로필사진,닉네임 표시되도록 변경
            .setItems(options) { dialog, which ->
                when (options[which]) {
                    "프로필 보기" -> {
                        // 프로필 보기 로직
                        val intent = Intent(activity, ProfileActivity::class.java)
                        intent.putExtra("nick", user.nick)
                        intent.putExtra("uId", user.uId)
                        startActivity(intent)
                    }
                    "대화하기" -> {
                        // 대화하기 로직
                        val intent = Intent(requireContext(), ChatActivity::class.java)
                        intent.putExtra("nick", user.nick)
                        intent.putExtra("uId", user.uId)
                        intent.putExtra("profileImageUrl", user.profileImageUrl)
                        startActivity(intent)
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

}
