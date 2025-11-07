package com.han.reservation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.han.reservation.databinding.ActivityGuideTourListBinding

class GuideTourListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuideTourListBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var tourAdapter: GuideTourAdapter
    private val tourList = mutableListOf<Tour>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuideTourListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("tours")

        setupRecyclerView()
        loadMyTours()
    }

    private fun setupRecyclerView() {
        tourAdapter = GuideTourAdapter(tourList) { tour ->
            val intent = Intent(this, TourDetailActivity::class.java)
            intent.putExtra("tourId", tour.id)
            startActivity(intent)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@GuideTourListActivity)
            adapter = tourAdapter
        }
    }

    private fun loadMyTours() {
        val currentUser = auth.currentUser ?: return
        database.orderByChild("guideId").equalTo(currentUser.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    tourList.clear()
                    for (data in snapshot.children) {
                        val tour = data.getValue(Tour::class.java)
                        tour?.let { tourList.add(it) }
                    }
                    tourAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@GuideTourListActivity, "데이터 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
