package com.han.kkaTalk

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SettingFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var etNick: EditText
    private lateinit var btnSaveNick: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Fragment의 레이아웃을 inflate
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        // Firebase 인증 및 DB 초기화
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference

        // 뷰 초기화
        etNick = view.findViewById(R.id.et_nick)
        btnSaveNick = view.findViewById(R.id.btn_save_nick)

        // 현재 사용자 닉네임 로드
        loadUserNick()

        // 닉네임 저장 버튼 클릭 리스너
        btnSaveNick.setOnClickListener {
            val newNick = etNick.text.toString()
            if (newNick.isNotEmpty()) {
                saveNickToDatabase(newNick)
            } else {
                Toast.makeText(requireContext(), "닉네임을 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    // 사용자 닉네임 로드
    private fun loadUserNick() {
        val currentUser = mAuth.currentUser
        currentUser?.let {
            mDbRef.child("user").child(it.uid).child("nick")
                .get().addOnSuccessListener { snapshot ->
                    val nick = snapshot.getValue(String::class.java)
                    etNick.setText(nick)
                }
        }
    }

    // 닉네임을 DB에 저장
    private fun saveNickToDatabase(newNick: String) {
        val currentUser = mAuth.currentUser
        currentUser?.let {
            mDbRef.child("user").child(it.uid).child("nick").setValue(newNick)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "닉네임이 저장되었습니다", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "닉네임 저장 실패", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
