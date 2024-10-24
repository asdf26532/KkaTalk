package com.han.kkaTalk

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
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

        // 사용자 정보를 Firebase에서 가져오기
        fetchUserData()

        return binding.root
    }

    private fun fetchUserData() {
        mDbRef.child("user").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear() // 리스트 초기화
                for (postSnapshot in snapshot.children) {
                    val currentUser = postSnapshot.getValue(User::class.java)
                    if (currentUser != null && mAuth.currentUser?.uid != currentUser.uId) {
                        userList.add(currentUser)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // 오류 처리
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
                    }
                    "대화하기" -> {
                        // 대화하기 로직
                        val intent = Intent(requireContext(), ChatActivity::class.java)
                        intent.putExtra("name", user.name)
                        intent.putExtra("nick", user.nick)
                        intent.putExtra("uId", user.uId)
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
