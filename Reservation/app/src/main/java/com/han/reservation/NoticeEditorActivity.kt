package com.han.reservation

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NoticeEditorActivity : AppCompatActivity() {

    private lateinit var editTitle: EditText
    private lateinit var editContent: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userRef: DatabaseReference
    private lateinit var prefs: android.content.SharedPreferences

    private var userId: String = ""
    private var isAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice_editor)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        editTitle = findViewById(R.id.editTitle)
        editContent = findViewById(R.id.editContent)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnEdit = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)

        // userId 불러오기
        val currentUser = auth.currentUser
        userId = if (currentUser != null) {
            currentUser.uid.also { Log.d("NoticeEditor", "userid: $it") }
        } else {
            prefs.getString("userId", null) ?: "".also { Log.d("NoticeEditor", "userid: $it") }
        }

        userRef = database.getReference("user").child(userId)


        checkUserRoleAndSetupUI()
    }

    private fun checkUserRoleAndSetupUI() {
        userRef.child("role").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val role = snapshot.getValue(String::class.java)
                isAdmin = role == "admin"
                setupNoticeEditor()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@NoticeEditorActivity, "권한 확인 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupNoticeEditor() {
        val receivedTitle = intent.getStringExtra("notice_title")
        val receivedContent = intent.getStringExtra("notice_content")
        val noticeId = intent.getStringExtra("notice_id")
        Log.d("NoticeEditor", "받은 데이터 - 제목: $receivedTitle, 내용: $receivedContent, ID: $noticeId")

        if (receivedTitle != null && receivedContent != null) {
            // 보기 모드
            editTitle.setText(receivedTitle)
            editContent.setText(receivedContent)

            editTitle.isEnabled = false
            editContent.isEnabled = false
            btnSubmit.visibility = View.GONE

            if (isAdmin) {
                // 관리자만 수정/삭제 버튼 보이기
                btnEdit.visibility = View.VISIBLE
                btnDelete.visibility = View.VISIBLE

                btnEdit.setOnClickListener {
                    editTitle.isEnabled = true
                    editContent.isEnabled = true
                    btnSubmit.visibility = View.VISIBLE
                    btnEdit.visibility = View.GONE
                    btnDelete.visibility = View.GONE
                }

                btnDelete.setOnClickListener {
                    AlertDialog.Builder(this)
                        .setTitle("공지 삭제")
                        .setMessage("정말 이 공지를 삭제하시겠습니까?")
                        .setPositiveButton("예") { dialog, _ ->
                            val noticeId = intent.getStringExtra("notice_id")
                            if (noticeId != null) {
                                val ref = FirebaseDatabase.getInstance().getReference("notices").child(noticeId)
                                ref.removeValue()
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "공지 삭제 완료", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "공지 삭제 실패", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(this, "공지 ID 없음", Toast.LENGTH_SHORT).show()
                            }
                            dialog.dismiss()
                        }
                        .setNegativeButton("아니오") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }

                btnSubmit.setOnClickListener {
                    val newTitle = editTitle.text.toString().trim()
                    val newContent = editContent.text.toString().trim()

                    if (noticeId != null) {
                        updateNotice(noticeId, newTitle, newContent)
                    }
                }
            }
        } else {
            // 작성 모드 (관리자만 가능)
            if (!isAdmin) {
                Toast.makeText(this, "접근 권한이 없습니다.", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            btnSubmit.setOnClickListener {
                val title = editTitle.text.toString().trim()
                val content = editContent.text.toString().trim()


                if (title.isEmpty() || content.isEmpty()) {
                    Toast.makeText(this, "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
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
                showCustomToast("공지사항이 등록되었습니다.")
                finish()
            }
            .addOnFailureListener {
                showCustomToast("등록 실패: ${it.message}")
            }
    }

    // 공지 수정
    private fun updateNotice(noticeId: String, newTitle: String, newContent: String) {
        val updates = mapOf(
            "title" to newTitle,
            "content" to newContent
        )

        database.reference.child("notices").child(noticeId)
            .updateChildren(updates)
            .addOnSuccessListener {
                showCustomToast("공지사항이 수정되었습니다.")
                finish()
            }
            .addOnFailureListener {
                showCustomToast("수정 실패: ${it.message}")
            }
    }

    // 공지 삭제
    private fun deleteNotice(noticeId: String) {
        database.reference.child("notices").child(noticeId)
            .removeValue()
            .addOnSuccessListener {
                showCustomToast("공지사항이 삭제되었습니다.")
                finish()
            }
            .addOnFailureListener {
                showCustomToast("삭제 실패: ${it.message}")
            }
    }

    private fun showCustomToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
