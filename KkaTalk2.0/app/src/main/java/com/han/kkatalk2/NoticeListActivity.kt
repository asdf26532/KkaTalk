package com.han.kkatalk2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class NoticeListActivity : AppCompatActivity() {
    private lateinit var noticeAdapter: NoticeAdapter
    private val noticeList = mutableListOf<Notice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice_list)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewNotice)
        recyclerView.layoutManager = LinearLayoutManager(this)
        noticeAdapter = NoticeAdapter(noticeList) { notice ->
            val intent = Intent(this, NoticeEditorActivity::class.java)
            intent.putExtra("notice_id", notice.noticeId)
            intent.putExtra("notice_title", notice.title)
            intent.putExtra("notice_content", notice.content)

            startActivity(intent)
        }
        recyclerView.adapter = noticeAdapter

        loadAllNotices()
    }

    override fun onResume() {
        super.onResume()
        loadAllNotices()
    }

    private fun loadAllNotices() {
        val ref = FirebaseDatabase.getInstance().getReference("notices")
        ref.orderByChild("timestamp").get()
            .addOnSuccessListener { snapshot ->
                Log.d("NoticeDebug", "총 공지 수: ${snapshot.childrenCount}")
                noticeList.clear()
                for (child in snapshot.children) {
                    val notice = child.getValue(Notice::class.java)
                    Log.d("NoticeDebug", "불러온 공지: ${notice?.title}")
                    if (notice != null) noticeList.add(notice)
                }
                noticeList.reverse() // 최신순 정렬
                noticeAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { error ->
                Log.e("NoticeDebug", "공지 불러오기 실패: ${error.message}", error)
            }
    }
}