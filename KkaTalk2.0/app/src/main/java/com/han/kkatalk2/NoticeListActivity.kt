package com.han.kkatalk2

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import kotlin.math.ceil

class NoticeListActivity : AppCompatActivity() {

    private lateinit var noticeAdapter: NoticeAdapter
    private val allNotices = mutableListOf<Notice>()
    private val noticeList = mutableListOf<Notice>()

    private var currentPage = 1
    private val pageSize = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice_list)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewNotice)
        recyclerView.layoutManager = LinearLayoutManager(this)

        noticeAdapter = NoticeAdapter(noticeList) { notice ->
            val intent = Intent(this, NoticeEditorActivity::class.java).apply {
                putExtra("notice_id", notice.noticeId)
                putExtra("notice_title", notice.title)
                putExtra("notice_content", notice.content)
            }
            startActivity(intent)
        }
        recyclerView.adapter = noticeAdapter

        loadAllNotices()
    }

    private fun loadAllNotices() {
        val ref = FirebaseDatabase.getInstance().getReference("notices")
        ref.orderByChild("timestamp").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allNotices.clear()
                for (child in snapshot.children) {
                    val notice = child.getValue(Notice::class.java)
                    if (notice != null) allNotices.add(notice)
                }
                allNotices.reverse() // 최신순 정렬
                displayPage(1)
                setupPaginationButtons()
            }

            override fun onCancelled(error: DatabaseError) {
                showCustomToast("불러오기 실패")
            }
        })
    }

    private fun displayPage(page: Int) {
        currentPage = page
        val start = (page - 1) * pageSize
        val end = minOf(start + pageSize, allNotices.size)
        val visibleList = allNotices.subList(start, end)

        noticeList.clear()
        noticeList.addAll(visibleList)
        noticeAdapter.notifyDataSetChanged()
    }

    private fun setupPaginationButtons() {
        val container = findViewById<LinearLayout>(R.id.paginationContainer)
        container.removeAllViews()
        val totalPages = ceil(allNotices.size / pageSize.toDouble()).toInt()

        for (i in 1..totalPages) {
            val button = Button(this).apply {
                text = i.toString()
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 8
                }
                setBackgroundColor(Color.TRANSPARENT)
                setPadding(20, 10, 20, 10)

                setOnClickListener { displayPage(i) }
            }
            container.addView(button)
        }
    }
}