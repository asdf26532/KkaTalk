package com.han.reservation

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.database.*
import com.google.firebase.database.R
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class NoticeListActivity : AppCompatActivity() {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var noticeAdapter: NoticeAdapter
    private val allNotices = mutableListOf<Notice>()
    private val noticeList = mutableListOf<Notice>()

    private var currentPage = 1
    private val pageSize = 5
    private var totalPages = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice_list)

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        swipeRefreshLayout.setOnRefreshListener {
            loadAllNotices()
        }

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

        // 툴바 뒤로가기
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun loadAllNotices() {
        val ref = FirebaseDatabase.getInstance().getReference("notices")
        ref.orderByChild("timestamp").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val previousPage = currentPage // 기존 페이지 저장

                allNotices.clear()
                for (child in snapshot.children) {
                    val notice = child.getValue(Notice::class.java)
                    if (notice != null) allNotices.add(notice)
                }
                allNotices.reverse() // 최신순 정렬


                // 페이지 수 재계산
                totalPages = ceil(allNotices.size / pageSize.toDouble()).toInt()
                if (totalPages < 1) totalPages = 1

                // 페이지 범위 보정
                currentPage = min(previousPage, totalPages)

                displayPage(currentPage)

                swipeRefreshLayout.isRefreshing = false
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
        val container = findViewById<LinearLayout>(R.id.paginationLayout)
        container.removeAllViews()

        fun createButton(text: String, enabled: Boolean = true, onClick: (() -> Unit)? = null): Button {
            val button = Button(this).apply {
                this.text = text
                textSize = 14f
                isEnabled = enabled
                setPadding(8, 4, 8, 4)
                setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    setMargins(4, 0, 4, 0)
                }
            }
            onClick?.let { button.setOnClickListener { it() } }
            return button
        }

        // << 첫 페이지
        container.addView(createButton("≪") {
            displayPage(1)
        })

        // < 이전 페이지
        container.addView(createButton("<") {
            displayPage(max(1, currentPage - 1))
        })

        // 페이지 버튼 계산 (최대 3개)
        val maxVisiblePages = 3
        var startPage = max(1, currentPage - 1)
        var endPage = min(totalPages, startPage + maxVisiblePages - 1)

        if (endPage - startPage < maxVisiblePages - 1) {
            startPage = max(1, endPage - maxVisiblePages + 1)
        }

        // 앞쪽 생략 표시 (...)
        if (startPage > 2) {
            container.addView(createButton("...").apply { isEnabled = false })
        }

        // 페이지 숫자 버튼
        for (i in startPage..endPage) {
            val button = createButton(i.toString(), i != currentPage) {
                displayPage(i)
            }
            if (i == currentPage) {
                button.setTypeface(null, Typeface.BOLD)
                button.setBackgroundResource(R.drawable.current_page_background)
            }
            container.addView(button)
        }

        // 뒤쪽 생략 표시 (...)
        if (endPage < totalPages - 1) {
            container.addView(createButton("...").apply { isEnabled = false })
        }

        // > 다음 페이지
        container.addView(createButton(">") {
            displayPage(min(totalPages, currentPage + 1))
        })

        // >> 마지막 페이지
        container.addView(createButton("≫") {
            displayPage(totalPages)
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}
