package com.han.kkatalk2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class NoticeEditorActivity : AppCompatActivity() {

    private lateinit var editTitle: EditText
    private lateinit var editContent: EditText
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice_editor)

        editTitle = findViewById(R.id.editTitle)
        editContent = findViewById(R.id.editContent)
        btnSubmit = findViewById(R.id.btnSubmitNotice)

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

    private fun postNotice(title: String, content: String) {
        val noticeId =
            FirebaseDatabase.getInstance().reference.child("notices").push().key ?: return
        val notice = mapOf(
            "noticeId" to noticeId,
            "title" to title,
            "content" to content,
            "timestamp" to System.currentTimeMillis()
        )

        FirebaseDatabase.getInstance().reference.child("notices").child(noticeId)
            .setValue(notice)
            .addOnSuccessListener {
                showCustomToast("공지사항이 등록되었습니다.")
                finish()
            }
            .addOnFailureListener {
                showCustomToast("등록 실패: ${it.message}")
            }
    }

}