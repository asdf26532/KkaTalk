package com.han.reservation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.han.reservation.databinding.ActivityTourListBinding

class TourListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTourListBinding
    private lateinit var adapter: TourAdapter
    private val tourList = mutableListOf<Tour>()
    private val db = FirebaseDatabase.getInstance().reference.child("tours")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTourListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = TourAdapter(tourList) { selectedTour ->
            val intent = Intent(this, TourDetailActivity::class.java)
            intent.putExtra("tourId", selectedTour.id)
            startActivity(intent)
        }

        binding.recyclerViewTours.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewTours.adapter = adapter

        loadTours()
    }

    private fun loadTours() {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tourList.clear()
                for (tourSnap in snapshot.children) {
                    val tour = tourSnap.getValue(Tour::class.java)
                    if (tour != null) tourList.add(tour)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}