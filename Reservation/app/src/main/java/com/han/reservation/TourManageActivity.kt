package com.han.reservation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TourManageActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TourAdapter
    private lateinit var tourList: MutableList<Tour>
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour_manage)

        recyclerView = findViewById(R.id.recyclerTourManage)
        recyclerView.layoutManager = LinearLayoutManager(this)

        tourList = mutableListOf()
        adapter = TourAdapter(tourList) { tour ->
            // 클릭 시 수정화면으로 이동
            val intent = Intent(this, TourEditActivity::class.java)
            intent.putExtra("tourId", tour.tourId)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("tours")

        loadMyTours()

        findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btnAddTour).setOnClickListener {
            startActivity(Intent(this, TourEditActivity::class.java))
        }
    }

    private fun loadMyTours() {
        val uid = auth.currentUser?.uid ?: return
        database.orderByChild("guideId").equalTo(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    tourList.clear()
                    for (child in snapshot.children) {
                        val tour = child.getValue(Tour::class.java)
                        if (tour != null) tourList.add(tour)
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onResume() {
        super.onResume()
        loadMyTours()
    }
}