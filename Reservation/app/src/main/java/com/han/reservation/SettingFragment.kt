package com.han.reservation

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.BuildConfig
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.R
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
            requireContext().showCustomToast("사용자 정보를 불러올 수 없습니다")
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
    }