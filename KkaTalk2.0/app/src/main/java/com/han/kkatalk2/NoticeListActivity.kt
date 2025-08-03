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

        // << 버튼
        val firstButton = createNavButton("<<") {
            displayPage(1)
        }
        container.addView(firstButton)

        // < 버튼
        val prevButton = createNavButton("<") {
            if (currentPage > 1) displayPage(currentPage - 1)
        }
        container.addView(prevButton)

        // 페이지 숫자 (최대 3개만 보여주기)
        val pagesToShow = getVisiblePageNumbers(currentPage, totalPages)
        for (page in pagesToShow) {
            val button = Button(this).apply {
                text = if (page == -1) "..." else page.toString()
                setBackgroundColor(Color.TRANSPARENT)
                setPadding(20, 10, 20, 10)
                setTextColor(if (page == currentPage) Color.BLACK else Color.GRAY)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = 8
                    marginEnd = 8
                }
                if (page != -1) {
                    setOnClickListener { displayPage(page) }
                }
            }
            container.addView(button)
        }

        // > 버튼
        val nextButton = createNavButton(">") {
            if (currentPage < totalPages) displayPage(currentPage + 1)
        }
        container.addView(nextButton)

        // >> 버튼
        val lastButton = createNavButton(">>") {
            displayPage(totalPages)
        }
        container.addView(lastButton)
    }

    private fun createNavButton(text: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(20, 10, 20, 10)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = 8
                marginEnd = 8
            }
            setOnClickListener { onClick() }
        }
    }

    private fun getVisiblePageNumbers(current: Int, total: Int): List<Int> {
        return when {
            total <= 3 -> (1..total).toList()
            current <= 2 -> listOf(1, 2, 3, -1)
            current >= total - 1 -> listOf(-1, total - 2, total - 1, total)
            else -> listOf(-1, current - 1, current, current + 1, -1)
        }
    }

}