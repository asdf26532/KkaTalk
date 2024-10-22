package com.han.kkaTalk

import android.content.Intent
import android.os.Bundle
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
            showPopupMenu(user)
        }
    }

    private fun showPopupMenu(user: User) {
        val popupMenu = PopupMenu(requireContext(), binding.rvUser) // 팝업 메뉴의 anchor는 회원 리스트가 표시되는 리사이클러뷰로 설정
        popupMenu.menuInflater.inflate(R.menu.user_options_menu, popupMenu.menu) // 메뉴 리소스를 inflate

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.view_profile -> {
                    // 프로필 보기 기능
                    Toast.makeText(requireContext(), "${user.nick}의 프로필 보기", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.start_chat -> {
                    // 대화하기 기능
                    val intent = Intent(activity, ChatActivity::class.java)
                    intent.putExtra("name", user.name)
                    intent.putExtra("nick", user.nick)
                    intent.putExtra("uId", user.uId)
                    startActivity(intent)
                    true
                }
                R.id.cancel -> {
                    // 취소 버튼
                    popupMenu.dismiss()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

}
