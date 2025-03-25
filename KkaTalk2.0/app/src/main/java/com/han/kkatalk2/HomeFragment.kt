package com.han.kkatalk2

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var guideAdapter: GuideAdapter
    private var guideList = mutableListOf<Guide>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val editTextCity = view.findViewById<EditText>(R.id.edt_city)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_guide)
        val btnAddGuide = view.findViewById<FloatingActionButton>(R.id.btn_add_guide)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        guideAdapter = GuideAdapter(guideList)
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
                guideAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // 검색 기능: 지역 필터링
        editTextCity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val filteredList = guideList.filter { it.locate.contains(s.toString(), ignoreCase = true) }
                guideAdapter = GuideAdapter(filteredList)
                recyclerView.adapter = guideAdapter
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnAddGuide.setOnClickListener {
            val intent = Intent(requireContext(), RegisterGuideActivity::class.java)
            startActivity(intent)
        }




        return view
    }
}
