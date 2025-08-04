package com.han.kkatalk2

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class NoticeListActivity : AppCompatActivity() {

    private lateinit var noticeAdapter: NoticeAdapter
    private val allNotices = mutableListOf<Notice>()
    private val noticeList = mutableListOf<Notice>()

    private var currentPage = 1
    private val pageSize = 5
    private var totalPages = 1

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

                totalPages = ceil(allNotices.size / pageSize.toDouble()).toInt()

                displayPage(1)
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

        setupPaginationButtons()
    }

    private fun setupPaginationButtons() {
        val container = findViewById<LinearLayout>(R.id.paginationContainer)
        container.removeAllViews()

        val maxVisiblePages = 3
        var startPage = max(1, currentPage - 1)
        var endPage = min(totalPages, startPage + maxVisiblePages - 1)

        if (endPage - startPage < maxVisiblePages - 1) {
            startPage = max(1, endPage - maxVisiblePages + 1)
        }

        // < 버튼
        val prevButton = createPageButton("<").apply {
            isEnabled = currentPage > 1
            setOnClickListener { if (currentPage > 1) displayPage(currentPage - 1) }
        }
        container.addView(prevButton)

        // 숫자 버튼
        for (i in startPage..endPage) {
            val button = createPageButton(i.toString())
            if (i == currentPage) {
                button.setTypeface(null, Typeface.BOLD)
                button.setBackgroundResource(R.drawable.current_page_background)
            } else {
                button.setOnClickListener { displayPage(i) }
            }
            container.addView(button)
        }

        // ... + 마지막 페이지 버튼
        if (endPage < totalPages) {
            val ellipsis = createPageButton("...").apply { isEnabled = false }
            container.addView(ellipsis)

            val lastPageButton = createPageButton(totalPages.toString()).apply {
                setOnClickListener { displayPage(totalPages) }
            }
            container.addView(lastPageButton)
        }

        // > 버튼
        val nextButton = createPageButton(">").apply {
            isEnabled = currentPage < totalPages
            setOnClickListener { if (currentPage < totalPages) displayPage(currentPage + 1) }
        }
        container.addView(nextButton)
    }

    private fun createPageButton(text: String): Button {
        return Button(this).apply {
            this.text = text
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            setPadding(20, 10, 20, 10)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = 8
                marginEnd = 8
            }
        }
    }
}
