package com.han.reservation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TourAdapter
    private lateinit var database: DatabaseReference
    private val tourList = mutableListOf<Ttour>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerTours)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = TourAdapter(tourList) { tour ->
            val intent = Intent(requireContext(), TourDetailActivity::class.java)
            intent.putExtra("tourId", tour.id)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        database = FirebaseDatabase.getInstance().reference
        loadTours()

        return view
    }

    private fun loadTours() {
        database.child("tours")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    tourList.clear()
                    for (data in snapshot.children) {
                        val tour = data.getValue(Tour::class.java)
                        if (tour != null) {
                            tour.id = data.key ?: ""
                            tourList.add(tour)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
