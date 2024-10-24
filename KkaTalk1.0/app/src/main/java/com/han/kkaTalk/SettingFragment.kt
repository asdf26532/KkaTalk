package com.han.kkaTalk

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SettingFragment : Fragment() {

    private lateinit var tvCurrentNick: TextView
    private lateinit var btnChangeNick: Button
    private lateinit var edtNewNick: EditText
    private lateinit var btnSaveNewNick: Button
    private lateinit var btnLogout: Button

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

    private fun loadCurrentNick() {
        val userId = mAuth.currentUser?.uid
        if (userId != null) {
            mDbRef.child("user").child(userId).get().addOnSuccessListener { snapshot ->
                val currentUser = snapshot.getValue(User::class.java)
                tvCurrentNick.text = "현재 닉네임: ${currentUser?.nick}"
            }
        }
    }

    private fun updateNickname(newNick: String) {
        val userId = mAuth.currentUser?.uid
        if (userId != null) {
            mDbRef.child("user").child(userId).child("nick").setValue(newNick).addOnSuccessListener {
                tvCurrentNick.text = "현재 닉네임: $newNick"
                edtNewNick.visibility = View.GONE
                btnSaveNewNick.visibility = View.GONE
            }
        }
    }
}
