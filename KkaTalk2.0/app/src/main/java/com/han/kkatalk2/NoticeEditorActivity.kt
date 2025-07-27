package com.han.kkatalk2

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.han.kkatalk2.util.PreferenceUtil

class NoticeEditorActivity : AppCompatActivity() {

    private lateinit var editTitle: EditText
    private lateinit var editContent: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var prefs: PreferenceUtil

    private lateinit var userId: String
    private lateinit var userRef: DatabaseReference

    private var isAdmin: Boolean = false
    private var noticeId: String? = null // 삭제 시 필요

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice_editor)

        editTitle = findViewById(R.id.editTitle)
        editContent = findViewById(R.id.editContent)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnEdit = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        prefs = PreferenceUtil(this)

        // 유저 ID 가져오기
        val currentUser = auth.currentUser
        userId = if (currentUser != null) {
            currentUser.uid.also { Log.d("NoticeEditor", "userId: $it") }
        } else {
            prefs.getString("userId", null) ?: ""
        }

        // 유저 role 확인
        userRef = database.getReference("user").child(userId)
        userRef.child("role").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val role = snapshot.getValue(String::class.java)
                isAdmin = role == "admin"
                setupMode()
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("권한 확인 실패: ${error.message}")
                setupMode() // 실패해도 최소한 보기/작성 모드는 보여야 함
            }
        })
    }

    private fun setupMode() {
        val receivedTitle = intent.getStringExtra("notice_title")
        val receivedContent = intent.getStringExtra("notice_content")
        noticeId = intent.getStringExtra("notice_id") // 삭제 시 필요

        if (receivedTitle != null && receivedContent != null) {
            // 📌 보기 모드
            editTitle.setText(receivedTitle)
            editContent.setText(receivedContent)

            editTitle.isEnabled = false
            editContent.isEnabled = false
            btnSubmit.visibility = View.GONE

            if (isAdmin) {
                btnEdit.visibility = View.VISIBLE
                btnDelete.visibility = View.VISIBLE

                btnEdit.setOnClickListener {
                    editTitle.isEnabled = true
                    editContent.isEnabled = true
                    btnSubmit.visibility = View.VISIBLE
                    btnEdit.visibility = View.GONE
                }

                btnDelete.setOnClickListener {
                    deleteNotice()
                }

                btnSubmit.setOnClickListener {
                    val title = editTitle.text.toString().trim()
                    val content = editContent.text.toString().trim()
                    if (title.isEmpty() || content.isEmpty()) {
                        showToast("제목과 내용을 입력해주세요.")
                        return@setOnClickListener
                    }
                    updateNotice(title, content)
                }
            }
        } else {
            // ✏️ 작성 모드
            btnSubmit.visibility = View.VISIBLE
            btnSubmit.setOnClickListener {
                val title = editTitle.text.toString().trim()
                val content = editContent.text.toString().trim()

                if (title.isEmpty() || content.isEmpty()) {
                    showToast("제목과 내용을 입력해주세요.")
                    return@setOnClickListener
                }
                postNotice(title, content)
            }
        }
    }

    private fun postNotice(title: String, content: String) {
        val noticeId = database.reference.child("notices").push().key ?: return
        val notice = mapOf(
            "noticeId" to noticeId,
            "title" to title,
            "content" to content,
            "timestamp" to System.currentTimeMillis()
        )

        database.reference.child("notices").child(noticeId)
            .setValue(notice)
            .addOnSuccessListener {
                showToast("공지사항이 등록되었습니다.")
                finish()
            }
            .addOnFailureListener {
                showToast("등록 실패: ${it.message}")
            }
    }

    private fun updateNotice(title: String, content: String) {
        val id = noticeId ?: return
        val updated = mapOf(
            "title" to title,
            "content" to content
        )

        database.reference.child("notices").child(id)
            .updateChildren(updated)
            .addOnSuccessListener {
                showToast("공지사항이 수정되었습니다.")
                finish()
            }
            .addOnFailureListener {
                showToast("수정 실패: ${it.message}")
            }
    }

    private fun deleteNotice() {
        val id = noticeId ?: return
        database.reference.child("notices").child(id)
            .removeValue()
            .addOnSuccessListener {
                showToast("공지사항이 삭제되었습니다.")
                finish()
            }
            .addOnFailureListener {
                showToast("삭제 실패: ${it.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
