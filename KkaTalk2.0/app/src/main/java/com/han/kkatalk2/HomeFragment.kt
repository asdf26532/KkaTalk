package com.han.kkatalk2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var guideAdapter: GuideAdapter
    private var guideList = mutableListOf<Guide>()
    private var filteredList = mutableListOf<Guide>()

    private lateinit var spinnerCity: Spinner
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        spinnerCity = view.findViewById(R.id.spinner_city)
        recyclerView = view.findViewById(R.id.rv_guide)
        val btnAddGuide = view.findViewById<FloatingActionButton>(R.id.btn_add_guide)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        guideAdapter = GuideAdapter(filteredList)
        recyclerView.adapter = guideAdapter

        database = FirebaseDatabase.getInstance().getReference("guide")

        // Firebase에서 가이드 리스트 가져오기
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                guideList.clear()
                for (guideSnapshot in snapshot.children) {
                    val guide = guideSnapshot.getValue(Guide::class.java)
                    if (guide != null) {
                        guideList.add(guide)
                    }
                }
                // 시간순 정렬
                guideList.sortByDescending { it.timestamp }

                updateList()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // 공지 사항
        loadLatestNotice()

        // Spinner 셋업
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.city_list,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCity.adapter = adapter
        }

        // 도시 선택 리스너
        spinnerCity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                updateList()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 가이드 등록 버튼
        btnAddGuide.setOnClickListener {
            val intent = Intent(requireContext(), RegisterGuideActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    // 필터링된 리스트로 갱신
    private fun updateList() {
        val selectedCity = spinnerCity.selectedItem.toString()
        filteredList.clear()

        if (selectedCity == "전체") {
            filteredList.addAll(guideList)
        } else {
            filteredList.addAll(guideList.filter {
                it.locate.contains(selectedCity, ignoreCase = true)
            })
        }

        guideAdapter.notifyDataSetChanged()
    }

    // 최신 공지사항 불러오기
    private fun loadLatestNotice() {
        val noticeRef = FirebaseDatabase.getInstance().getReference("notices")
        noticeRef.orderByChild("timestamp").limitToLast(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val latestSnapshot = snapshot.children.first()
                    val latestNotice = latestSnapshot.getValue(Notice::class.java)
                    val noticeKey = latestSnapshot.key

                    latestNotice?.let {
                        // SharedPreferences에 저장된 닫힌 공지 키를 확인
                        val prefs = requireContext().getSharedPreferences("notice_prefs", 0)
                        val dismissedKey = prefs.getString("dismissed_notice_key", null)

                        // 만약 닫은 공지와 다르면 보여주기
                        if (noticeKey != dismissedKey) {
                            showNoticeBanner(it, noticeKey)
                        } else {
                            hideNoticeBanner()
                        }
                    }
                } else {
                    hideNoticeBanner()
                }
            }
            .addOnFailureListener {
                hideNoticeBanner()
            }
    }

    // 공지 배너
    private fun showNoticeBanner(notice: Notice, noticeKey: String?) {
        val bannerLayout = view?.findViewById<View>(R.id.noticeBanner)
        val titleText = view?.findViewById<TextView>(R.id.tvNoticeTitle)
        val contentText = view?.findViewById<TextView>(R.id.tvNoticeContent)
        val btnClose = view?.findViewById<ImageButton>(R.id.btnCloseNotice)

        bannerLayout?.visibility = View.VISIBLE
        titleText?.text = notice.title
        contentText?.text = notice.content

        btnClose?.setOnClickListener {
            // 닫은 공지 키 저장
            noticeKey?.let {
                val prefs = requireContext().getSharedPreferences("notice_prefs", 0)
                prefs.edit().putString("dismissed_notice_key", it).apply()
            }
            bannerLayout?.visibility = View.GONE
        }
    }

    // 공지 배너 숨기기
    private fun hideNoticeBanner() {
        view?.findViewById<View>(R.id.noticeBanner)?.visibility = View.GONE
    }
}
