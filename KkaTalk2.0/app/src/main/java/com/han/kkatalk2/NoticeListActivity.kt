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
        val container = findViewById<LinearLayout>(R.id.paginationLayout)
        container.removeAllViews()


        fun createButton(text: String, enabled: Boolean = true, onClick: (() -> Unit)? = null): Button {
            val button = Button(this).apply {
                this.text = text
                textSize = 14f
                isEnabled = enabled
                setPadding(20, 10, 20, 10)
                setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 0, 8, 0)
                }
            }
            onClick?.let { button.setOnClickListener { it() } }
            return button
        }

        if (currentPage > 1) {
            container.addView(createButton("<") {
                displayPage(currentPage - 1)
            })
        }

        val maxVisiblePages = 3
        var startPage = max(1, currentPage - 1)
        var endPage = min(totalPages, startPage + maxVisiblePages - 1)

        if (endPage - startPage < maxVisiblePages - 1) {
            startPage = max(1, endPage - maxVisiblePages + 1)
        }

        if (startPage > 1) {
            container.addView(createButton("1") { displayPage(1) })
            if (startPage > 2) {
                container.addView(createButton("...").apply { isEnabled = false })
            }
        }

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

        if (endPage < totalPages) {
            if (endPage < totalPages - 1) {
                container.addView(createButton("...").apply { isEnabled = false })
            }
            container.addView(createButton(totalPages.toString()) { displayPage(totalPages) })
        }

        if (currentPage < totalPages) {
            container.addView(createButton(">") {
                displayPage(currentPage + 1)
            })
        }
    }

}
