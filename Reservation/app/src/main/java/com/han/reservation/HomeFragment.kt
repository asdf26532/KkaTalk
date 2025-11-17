package com.han.reservation

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.google.firebase.database.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var database: DatabaseReference
    private lateinit var guideAdapter: GuideAdapter
    private var guideList = mutableListOf<Guide>()
    private var filteredList = mutableListOf<Guide>()

    private var isSearching = false

    private lateinit var spinnerCity: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var ivSearch: ImageView

    private lateinit var spinnerSort: Spinner

    private var currentSortType: String = "최신순"

    private var dismissedNoticeKeyInSession: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        spinnerCity = view.findViewById(R.id.spinner_city)
        recyclerView = view.findViewById(R.id.rv_guide)
        ivSearch = view.findViewById(R.id.iv_search)
        spinnerSort = view.findViewById(R.id.spinner_sort)
        val btnAddGuide = view.findViewById<FloatingActionButton>(R.id.btn_add_guide)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        guideAdapter = GuideAdapter(filteredList)
        recyclerView.adapter = guideAdapter

        database = FirebaseDatabase.getInstance().getReference("guide")

        // SwipeRefresh 새로고침 동작
        swipeRefreshLayout.setOnRefreshListener {
            loadGuideList()
        }

        // 첫 로딩
        loadGuideList()

        // 공지 사항
        loadLatestNotice()

        // 도시 선택 스피너 어댑터
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.home_city_list,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCity.adapter = adapter
        }

        // 도시 선택 리스너
        spinnerCity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                updateList()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 정렬 스피너 어댑터
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.sort_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSort.adapter = adapter
        }

        // 정렬 선택 리스너
        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                currentSortType = parent?.getItemAtPosition(position).toString()
                updateList() // 정렬 반영
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 검색 아이콘 클릭 → 검색 다이얼로그
        ivSearch.setOnClickListener {
            if (isSearching) {
                // 검색 취소 → 원래 리스트 복원
                filteredList.clear()
                filteredList.addAll(guideList)
                guideAdapter.highlightGuide("") // 하이라이트 초기화
                guideAdapter.notifyDataSetChanged()

                // 아이콘 돋보기로 변경
                ivSearch.setImageResource(R.drawable.ic_search)
                isSearching = false
            } else {
                // 검색 시작 → 다이얼로그 열기
                showSearchDialog()
            }
        }

        // 가이드 등록 버튼
        btnAddGuide.setOnClickListener {
            val intent = Intent(requireContext(), RegisterGuideActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadGuideList()
    }

    // Firebase에서 가이드 리스트 불러오기
    private fun loadGuideList() {
        swipeRefreshLayout.isRefreshing = true // 로딩 표시 시작

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                guideList.clear()
                for (guideSnapshot in snapshot.children) {
                    val guide = guideSnapshot.getValue(Guide::class.java)
                    if (guide != null) {
                        guideList.add(guide)
                    }
                }
                guideList.sortByDescending { it.timestamp }
                updateList()

                swipeRefreshLayout.isRefreshing = false // 로딩 표시 종료
            }

            override fun onCancelled(error: DatabaseError) {
                swipeRefreshLayout.isRefreshing = false // 에러 시 종료
            }
        })
    }

    // 필터링된 리스트로 갱신
    private fun updateList() {
        val selectedCity = spinnerCity.selectedItem.toString()
        filteredList.clear()

        when (selectedCity) {
            "전체" -> {
                filteredList.addAll(guideList)
            }
            "기타" -> {
                // home_city_list 배열에 있는 도시 목록 가져오기
                val cityArray = resources.getStringArray(R.array.home_city_list)
                    .filter { it != "전체" && it != "기타" }

                // locate 값이 배열에 있는 도시 중 아무것도 포함하지 않는 경우만 필터링
                filteredList.addAll(guideList.filter { guide ->
                    cityArray.none { cityName ->
                        guide.locate.contains(cityName, ignoreCase = true)
                    }
                })
            }
            else -> {
                filteredList.addAll(guideList.filter {
                    it.locate.contains(selectedCity, ignoreCase = true)
                })
            }
        }

        when (currentSortType) {
            "최신순" -> filteredList.sortByDescending { it.timestamp }
            "오래된순" -> filteredList.sortBy { it.timestamp }
            "조회순" -> filteredList.sortByDescending { it.viewCount }
        }

        guideAdapter.notifyDataSetChanged()
    }

    // 가이드 검색 다이얼로그
    private fun showSearchDialog() {
        val editText = EditText(requireContext())
        editText.hint = "가이드 검색"

        AlertDialog.Builder(requireContext())
            .setTitle("검색")
            .setView(editText)
            .setPositiveButton("검색") { _, _ ->
                filterGuideList(editText.text.toString())

                // 검색모드로 전환
                ivSearch.setImageResource(R.drawable.ic_cancel)
                isSearching = true
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 필터링
    private fun filterGuideList(query: String?) {
        val searchText = query?.lowercase()?.trim() ?: ""
        filteredList.clear()

        if (searchText.isEmpty()) {
            filteredList.addAll(guideList)
        } else {
            filteredList.addAll(
                guideList.filter {
                    it.title.lowercase().contains(searchText) ||
                            it.content.lowercase().contains(searchText)
                }
            )
        }
        guideAdapter.highlightGuide(searchText)
    }

    // 최신 공지사항 불러오기
    private fun loadLatestNotice() {
        val noticeRef = FirebaseDatabase.getInstance().getReference("notices")
        noticeRef.orderByChild("timestamp").limitToLast(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded || view == null) return@addOnSuccessListener  // Fragment가 살아있는지 확인

                Log.d("Notice", "Fragment not added or view is null. return")
                if (snapshot.exists()) {
                    val latestSnapshot = snapshot.children.first()
                    val latestNotice = latestSnapshot.getValue(Notice::class.java)
                    val noticeKey = latestSnapshot.key

                    latestNotice?.let {
                        val prefs = requireContext().getSharedPreferences("notice_prefs", 0)
                        val dontShowDate = prefs.getString("dont_show_notice_date", null)
                        val dontShowKey = prefs.getString("dont_show_notice_key", null)
                        val dismissedKey = prefs.getString("dismissed_notice_key_in_session", null)
                        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

                        Log.d("Notice", "Today=$today, don'tShow=$dontShowDate, dismissedKey=$dismissedNoticeKeyInSession, currentKey=$noticeKey")

                        // 오늘 하루 숨기기 체크
                        if (dontShowDate == today && noticeKey == dontShowKey) {
                            Log.d("Notice", "오늘 숨기기 설정됨. 배너 숨김")
                            hideNoticeBanner()
                            return@addOnSuccessListener
                        }

                        // 세션 중 닫은 배너
                        if (noticeKey == dismissedKey) {
                            Log.d("Notice", "세션 중 닫힌 공지와 같음. 배너 숨김")
                            hideNoticeBanner()
                            return@addOnSuccessListener
                        }

                        // 지연 실행으로 View가 완전히 준비된 이후 실행
                        view?.post {
                            Log.d("Notice", "배너 보여주기 실행됨")
                            showNoticeBanner(latestNotice, noticeKey)
                        }
                    }
                } else {
                    Log.d("Notice", "공지 없음")
                    hideNoticeBanner()
                }
            }
            .addOnFailureListener {
                Log.e("Notice", "공지 불러오기 실패", it)
                hideNoticeBanner()
            }
    }

    // 공지 배너
    private fun showNoticeBanner(notice: Notice, noticeKey: String?) {
        val bannerLayout = view?.findViewById<View>(R.id.noticeBanner)
        val titleText = view?.findViewById<TextView>(R.id.tvNoticeTitle)
        val contentText = view?.findViewById<TextView>(R.id.tvNoticeContent)
        val btnClose = view?.findViewById<Button>(R.id.btnCloseNotice)
        val btnDontShowToday = view?.findViewById<Button>(R.id.btnDontShowToday)

        if (bannerLayout == null || titleText == null || contentText == null) return

        titleText.text = notice.title
        contentText.text = notice.content

        bannerLayout.visibility = View.VISIBLE
        Log.d("Notice", "공지 배너 VISIBLE 상태로 변경됨")

        btnClose?.setOnClickListener {
            dismissedNoticeKeyInSession = noticeKey
            val prefs = requireContext().getSharedPreferences("notice_prefs", 0)
            prefs.edit().putString("dismissed_notice_key_in_session", noticeKey).apply()
            Log.d("Notice", "닫기 버튼 클릭됨. dismissedNoticeKeyInSession = $dismissedNoticeKeyInSession")
            bannerLayout.visibility = View.GONE
        }

        btnDontShowToday?.setOnClickListener {
            val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val prefs = requireContext().getSharedPreferences("notice_prefs", 0)
            prefs.edit()
                .putString("dont_show_notice_date", today)
                .putString("dont_show_notice_key", noticeKey) // 추가!
                .apply()
            Log.d("Notice", "오늘 숨기기 클릭됨. SharedPreferences 저장 완료")
            bannerLayout.visibility = View.GONE
        }
    }

    // 공지 배너 숨기기
    private fun hideNoticeBanner() {
        Log.d("Notice", "공지 배너 숨김 처리")
        view?.findViewById<View>(R.id.noticeBanner)?.visibility = View.GONE
    }

}
