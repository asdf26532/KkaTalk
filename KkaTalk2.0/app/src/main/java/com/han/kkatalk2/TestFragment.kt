package com.han.kkatalk2

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.han.kkatalk2.databinding.FragmentTestBinding

class TestFragment : Fragment() {
    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userRef: DatabaseReference
    private var userId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // 유저 ID 로드
        val currentUser = auth.currentUser
        if (currentUser != null) {
            userId = currentUser.uid
            Log.d("SettingFragment","userid: $userId")
        } else {
            // SharedPreferences에서 userid 가져오기
            val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            userId = sharedPref.getString("userId", null) ?: ""
            Log.d("SettingFragment","userid: $userId")
        }

        loadUserData()

        // 닉네임 변경 버튼 클릭 처리
        binding.btnChangeNick.setOnClickListener {
            val newNick = binding.edtNewNick.text.toString().trim()
            if (newNick.isNotEmpty() && userId.isNotEmpty()) {
                database.getReference("user").child(userId).child("nick").setValue(newNick)
                binding.tvCurrentNick.text = newNick
            }
        }
    }

    private fun loadUserData() {
        userRef = database.getReference("user").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nick = snapshot.child("nick").getValue(String::class.java) ?: "닉네임 없음"
                val status = snapshot.child("status").getValue(String::class.java) ?: "상태메시지 없음"
                val profileUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                binding.tvCurrentNick.text = nick
                binding.tvCurrentStatus.text = status

                if (!profileUrl.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(profileUrl)
                        .into(binding.ivProfile)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // 실패 처리
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}