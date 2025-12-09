package com.han.reservation

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.BuildConfig
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.R
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.han.reservation.databinding.FragmentSettingBinding


class SettingFragment : Fragment() {
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userRef: DatabaseReference
    private lateinit var prefs: SharedPreferences
    private lateinit var storage: FirebaseStorage
    private lateinit var progressBar: ProgressBar
    private var userId: String = ""

    private var adminClickCount = 0
    private var lastClickTime = 0L

    private val TAG = "SettingFragment"


    private val appPrefs by lazy {
        requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        progressBar = binding.progressBar

        // 유저 정보 불러오기
        val currentUser = auth.currentUser
        if (currentUser != null) {
            userId = currentUser.uid
            Log.d(TAG, "userid: $userId")
        } else {
            // SharedPreferences에서 userid 가져오기
            userId = prefs.getString("userId", null) ?: ""
            Log.d(TAG, "userid: $userId")
        }

        userRef = database.getReference("user").child(userId)

        if (userId.isNotEmpty()) {
            loadUserData()
        } else {
            // 로그인 화면으로 이동
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        // 닉네임 변경 버튼 클릭 시
        binding.btnChangeNick.setOnClickListener {
            binding.edtNewNick.visibility = View.VISIBLE
            binding.btnSaveNewNick.visibility = View.VISIBLE
        }

        // 닉네임 저장 버튼 클릭 시
        binding.btnSaveNewNick.setOnClickListener {
            val newNick = binding.edtNewNick.text.toString().trim()
            if (newNick.isNotEmpty()) {
                updateNickname(newNick)
            }
        }

        // 차단 관리
        binding.btnManageBlockedUsers.setOnClickListener {
            val intent = Intent(requireContext(), BlockedUsersActivity::class.java)
            startActivity(intent)
        }

        // 로그아웃
        binding.btnLogout.setOnClickListener {
            handleLogout()
        }

        // 회원 탈퇴
        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
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
                if (!isAdded || _binding == null) {
                    Log.e(TAG, "loadUserData() 호출 시 fragment가 detach 상태이거나 binding이 null입니다.")
                    return
                }

                val nick = snapshot.child("nick").getValue(String::class.java) ?: "닉네임 없음"
                val status = snapshot.child("statusMessage").getValue(String::class.java) ?: "상태메시지 없음"
                val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                binding.tvCurrentNick.text = "현재 닉네임: $nick"
                binding.progressBar.visibility = View.VISIBLE

                if (!profileImageUrl.isNullOrEmpty()) {
                    val storageRef = storage.getReferenceFromUrl(profileImageUrl)

                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        if (!isAdded || _binding == null) return@addOnSuccessListener

                        Glide.with(this@SettingFragment)
                            .load(uri)
                            .placeholder(R.drawable.profile_default)
                            .into(binding.ivProfile)

                    }.addOnFailureListener {
                        if (!isAdded || _binding == null) return@addOnFailureListener

                        binding.ivProfile.setImageResource(R.drawable.profile_default)
                    }.addOnCompleteListener {
                        if (!isAdded || _binding == null) return@addOnCompleteListener

                        binding.progressBar.visibility = View.GONE
                    }
                } else {
                    binding.ivProfile.setImageResource(R.drawable.profile_default)
                    binding.progressBar.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded || _binding == null) return
                requireContext().showCustomToast("사용자를 찾을 수 없습니다")
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
                    requireContext().showCustomToast("닉네임이 변경되었습니다")
                }
                .addOnFailureListener {
                    requireContext().showCustomToast("닉네임 변경에 실패했습니다.")
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

    // 로그아웃 후처리
    private fun redirectToLogin() {
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    // 탈퇴 확인
    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("회원 탈퇴")
            .setMessage("정말 탈퇴하시겠습니까? 모든 정보가 삭제됩니다.")
            .setPositiveButton("탈퇴") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 계정 삭제
    private fun deleteAccount() {
        if (userId.isNotEmpty()) {

            // 1. DB에서 유저 데이터 삭제
            userRef.removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // 2. auth계정 삭제 (자체 회원가입 유저인 경우만)
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        if (currentUser != null) {
                            currentUser.delete()
                                .addOnCompleteListener { authTask ->
                                    if (authTask.isSuccessful) {
                                        Toast.makeText(requireContext(), "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                                        FirebaseAuth.getInstance().signOut()
                                        redirectToLogin()
                                    } else {
                                        Toast.makeText(requireContext(), "계정 삭제 실패: ${authTask.exception?.message}.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            // SNS 로그인 사용자: DB만 삭제됨
                            Toast.makeText(requireContext(), "회원 데이터 삭제 완료되었습니다. (SNS 로그인 계정)", Toast.LENGTH_SHORT).show()
                            redirectToLogin()
                        }
                    } else {
                        Toast.makeText(requireContext(), "데이터베이스 삭제 실패", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(requireContext(), "로그인된 사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }


}