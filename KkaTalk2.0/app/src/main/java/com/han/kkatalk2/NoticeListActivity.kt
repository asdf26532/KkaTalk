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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NoticeListActivity : AppCompatActivity() {
    private lateinit var noticeAdapter: NoticeAdapter
    private val noticeList = mutableListOf<Notice>()

    private val pageSize = 10
    private var lastLoadedKey: String? = null
    private var isLoading = false
    private var isLastPage = false

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

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && !isLastPage && totalItemCount <= (lastVisibleItem + 2)) {
                    loadNotices()
                }
            }
        })

        loadNotices()

    }

    override fun onResume() {
        super.onResume()
        loadNotices()
    }

    private fun loadNotices() {
        isLoading = true
        val ref = FirebaseDatabase.getInstance().getReference("notices")
        var query = ref.orderByKey().limitToFirst(pageSize + 1) // +1은 다음 페이지 유무 확인용

        if (lastLoadedKey != null) {
            query = query.startAfter(lastLoadedKey!!)
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newItems = mutableListOf<Notice>()
                var count = 0
                var newLastKey: String? = null

                for (child in snapshot.children) {
                    val notice = child.getValue(Notice::class.java)
                    notice?.let {
                        if (count < pageSize) {
                            newItems.add(it)
                            newLastKey = child.key
                        }
                        count++
                    }
                }

                if (count <= pageSize) {
                    isLastPage = true
                }

                lastLoadedKey = newLastKey
                noticeList.addAll(newItems)
                noticeAdapter.notifyDataSetChanged()
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
                showCustomToast("불러오기 실패")

            }
        })
    }

   /* private fun loadAllNotices() {
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
    }*/
}