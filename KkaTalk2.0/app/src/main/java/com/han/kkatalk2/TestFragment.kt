package com.han.kkatalk2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.han.kkatalk2.databinding.FragmentTestBinding
import com.kakao.sdk.user.UserApiClient

class TestFragment : Fragment() {
    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userRef: DatabaseReference
    private lateinit var prefs: SharedPreferences
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
        prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // 유저 정보 불러오기
        val currentUser = auth.currentUser
        if (currentUser != null) {
            userId = currentUser.uid
            Log.d("SettingFragment","userid: $userId")
        } else {
            // SharedPreferences에서 userid 가져오기
            userId = prefs.getString("userId", null) ?: ""
            Log.d("SettingFragment","userid: $userId")
        }

        userRef = database.getReference("user").child(userId)


        // 유저 정보 불러오기
        if (userId.isNotEmpty()) {
            loadUserData()
        } else {
            Toast.makeText(requireContext(), "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_LONG).show()
            // 로그인 화면으로 이동
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        // 닉네임 변경 버튼 클릭 시
        binding.btnChangeNick.setOnClickListener {
            binding.edtNewNick.visibility= View.VISIBLE
            binding.btnSaveNewNick.visibility = View.VISIBLE
        }

        // 저장 버튼 클릭 시
        binding.btnSaveNewNick.setOnClickListener {
            val newNick = binding.edtNewNick.text.toString().trim()
            if (newNick.isNotEmpty()) {
                updateNickname(newNick)
            }
        }

        // 상태 메시지 변경 버튼 클릭 시
        binding.btnChangeStatus.setOnClickListener {
            binding.edtNewStatus.visibility = View.VISIBLE
            binding.btnSaveNewStatus.visibility = View.VISIBLE
        }

        // 상태 메시지 저장 버튼 클릭 시
        binding.btnSaveNewStatus.setOnClickListener {
            val newStatus = binding.edtNewStatus.text.toString().trim()
            if (newStatus.isNotEmpty()) {
                updateStatusMessage(newStatus)
            }
        }

        // 차단 관리 버튼 클릭 시
        binding.btnManageBlockedUsers.setOnClickListener {
            val intent = Intent(requireContext(), BlockedUsersActivity::class.java)
            startActivity(intent)
        }

        // 로그아웃 버튼 클릭 시
        binding.btnLogout.setOnClickListener {
            handleLogout()
        }



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 현재 사용자 정보 불러오기
    private fun loadUserData() {
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nick = snapshot.child("nick").getValue(String::class.java) ?: "닉네임 없음"
                val status = snapshot.child("status").getValue(String::class.java) ?: "상태메시지 없음"
                val profileUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                binding.tvCurrentNick.text = "현재 닉네임: $nick"
                binding.tvCurrentStatus.text = "현재 상태 메시지: $status"

                if (!profileUrl.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(profileUrl)
                        .into(binding.ivProfile)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // 실패 처리
                Toast.makeText(requireContext(), "사용자를 찾을 수 없습니다", Toast.LENGTH_LONG).show()
            }
        })
    }

    // 닉네임 변경
    private fun updateNickname(newNick: String) {
        if (userId.isNotEmpty()) {
            userRef.child("nick").setValue(newNick)
                .addOnSuccessListener {
                    binding.tvCurrentNick.text = "현재 닉네임: $newNick"
                    binding.edtNewNick.text.clear()
                    binding.edtNewNick.visibility = View.GONE
                    binding.btnSaveNewNick.visibility = View.GONE
                    Toast.makeText(requireContext(), "닉네임이 변경되었습니다.", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "닉네임 변경에 실패했습니다.", Toast.LENGTH_LONG).show()
                }
        }
    }

    // 상태 메시지 업데이트
    private fun updateStatusMessage(newStatus: String) {
        if (userId.isNotEmpty()) {
            userRef.child("statusMessage").setValue(newStatus)
                .addOnSuccessListener {
                    binding.tvCurrentStatus.text = "현재 상태 메시지: $newStatus"
                    binding.edtNewStatus.visibility = View.GONE
                    binding.btnSaveNewStatus.visibility = View.GONE
                }
        }
    }

    // 통합 로그아웃 처리
    private fun handleLogout() {
        // FirebaseAuth 로그아웃
        auth.signOut()

        // 카카오 로그아웃 시도 (카카오 로그아웃 실패해도 무시)
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Log.w("TestFragment", "카카오 로그아웃 실패: ${error.message}")
            }
            // SharedPreferences 초기화 및 로그인 화면 이동
            prefs.edit().clear().apply()
            redirectToLogin()
        }
    }


    private fun redirectToLogin() {
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }



}