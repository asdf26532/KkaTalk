package com.han.reservation

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.han.reservation.TourAdapter
import com.han.reservation.Tour


class TourListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: TourAdapter
    private val database = FirebaseDatabase.getInstance().reference
    private val tours = mutableListOf<Tour>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour_list)

        recyclerView = findViewById(R.id.recyclerTourList)
        progressBar = findViewById(R.id.progressBarTourList)

        adapter = TourAdapter(
            emptyList(),
            onItemClick = { tour ->
                // 상세보기로 이동
                val intent = Intent(this, TourDetailActivity::class.java)
                intent.putExtra("tourId", tour.id)
                startActivity(intent)
            },
            onEditClick = { tour ->
                // ✳ 수정 모드로 이동 ✳
                val intent = Intent(this, TourRegisterActivity::class.java)
                intent.putExtra("editMode", true)
                intent.putExtra("tourId", tour.id)
                startActivity(intent)
            },
            onDeleteClick = { tour ->
                deleteTour(tour)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadTours()
    }

    private fun loadTours() {
        progressBar.visibility = View.VISIBLE

        database.child("tours")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    tours.clear()

                    for (child in snapshot.children) {
                        val tour = child.getValue(Tour::class.java)
                        tour?.let {
                            it.id = child.key ?: ""
                            tours.add(it)
                        }
                    }

                    adapter.updateList(tours)
                    progressBar.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@TourListActivity, "불러오기 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun deleteTour(tour: Tour) {
        database.child("tours").child(tour.id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "투어가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                loadTours() // 새로고침
            }
            .addOnFailureListener {
                Toast.makeText(this, "삭제 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

}