package com.han.kkaTalk

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kakao.sdk.user.model.Profile

class SettingFragment : Fragment() {

    private lateinit var tvCurrentNick: TextView
    private lateinit var btnChangeNick: Button
    private lateinit var edtNewNick: EditText
    private lateinit var btnSaveNewNick: Button
    private lateinit var btnLogout: Button
    private lateinit var btnChangeProfile: Button
    private lateinit var ivProfile: ImageView

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference

        // UI 요소 초기화
        tvCurrentNick = view.findViewById(R.id.tv_current_nick)
        btnChangeNick = view.findViewById(R.id.btn_change_nick)
        edtNewNick = view.findViewById(R.id.edt_new_nick)
        btnSaveNewNick = view.findViewById(R.id.btn_save_new_nick)
        btnLogout = view.findViewById(R.id.btn_logout)
        btnChangeProfile = view.findViewById(R.id.btn_change_profile)
        ivProfile = view.findViewById(R.id.iv_profile)

        // 현재 프로필 사진 로드
        loadCurrentProfile()

        // 현재 닉네임 로드
        loadCurrentNick()

        // 닉네임 변경 버튼 클릭 시
        btnChangeNick.setOnClickListener {
            edtNewNick.visibility = View.VISIBLE

            btnSaveNewNick.visibility = View.VISIBLE
        }

        // 저장 버튼 클릭 시
        btnSaveNewNick.setOnClickListener {
            val newNick = edtNewNick.text.toString().trim()
            if (newNick.isNotEmpty()) {
                updateNickname(newNick)
            }
        }

        // 로그아웃 버튼 클릭 시
        btnLogout.setOnClickListener {
            mAuth.signOut()  // Firebase에서 로그아웃
            val intent = Intent(activity, LoginActivity::class.java) // 로그인 화면으로 이동
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // 기존 액티비티 스택을 모두 제거
            startActivity(intent)
            activity?.finish()  // 현재 액티비티 종료
        }

        return view
    }

    private fun loadCurrentProfile() {
        val userId = mAuth.currentUser?.uid.toString()

        if (userId.isNotEmpty()) {
            mDbRef.child("user").child(userId).get().addOnSuccessListener { snapshot ->
                val currentUser = snapshot.getValue(User::class.java)
                val profileImageUrl = currentUser?.profileImageUrl ?: "gs://kkatalk-cf3fd.appspot.com/profile_default.png"

                // Firebase Storage 참조 생성
                val storageRef = Firebase.storage.getReferenceFromUrl(profileImageUrl)

                // Firebase Storage에서 이미지 URL 다운로드
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Glide를 사용해 ImageView에 이미지 로드
                    Glide.with(this@SettingFragment)
                        .load(uri)
                        .placeholder(R.drawable.profile_default) // 기본 이미지 설정
                        .into(ivProfile)
                }.addOnFailureListener {
                    // 실패 시 기본 이미지 로드
                    ivProfile.setImageResource(R.drawable.profile_default)
                }
            }
        }
    }

    private fun loadCurrentNick() {
        val userId = mAuth.currentUser?.uid.toString()
        if (userId != null) {
            mDbRef.child("user").child(userId).get().addOnSuccessListener { snapshot ->
                val currentUser = snapshot.getValue(User::class.java)
                tvCurrentNick.text = "현재 닉네임: ${currentUser?.nick}"
            }
        }
    }

    private fun updateNickname(newNick: String) {
        val userId = mAuth.currentUser?.uid.toString()
        if (userId != null) {
            mDbRef.child("user").child(userId).child("nick").setValue(newNick).addOnSuccessListener {
                tvCurrentNick.text = "현재 닉네임: $newNick"
                edtNewNick.visibility = View.GONE
                btnSaveNewNick.visibility = View.GONE
            }
        }
    }
}
