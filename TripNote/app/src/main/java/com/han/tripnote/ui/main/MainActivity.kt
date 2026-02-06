package com.han.tripnote.ui.main

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.han.tripnote.R
import com.han.tripnote.data.model.Trip
import com.han.tripnote.ui.add.AddTripActivity
import com.han.tripnote.util.TripStorage
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import com.han.tripnote.ui.detail.TripDetailActivity

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: TripViewModel
    private lateinit var adapter: TripAdapter
    private lateinit var emptyLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.rvTripList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel = ViewModelProvider(this)[TripViewModel::class.java]

        adapter = TripAdapter(
            onItemClick = { trip ->
                val intent = Intent(this, TripDetailActivity::class.java)
                intent.putExtra("trip", trip)
                startActivity(intent)
            },
            onItemLongClick = { position ->
                showDeleteDialog(position)
            }
        )
        recyclerView.adapter = adapter

        emptyLayout = findViewById(R.id.layoutEmpty)

        viewModel.tripList.observe(this) { list ->
            adapter.submitList(list.toList())
            updateEmptyView(list)
        }

        viewModel.load(this)

        // 여행 추가 버튼
        findViewById<View>(R.id.fabAddTrip).setOnClickListener {
            val intent = Intent(this, AddTripActivity::class.java)
            startActivityForResult(intent, 100)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {

            val trip = Trip(
                id = data?.getStringExtra("id") ?: "",
                title = data?.getStringExtra("title") ?: "",
                location = data?.getStringExtra("location") ?: "",
                startDate = data?.getStringExtra("startDate") ?: "",
                endDate = data?.getStringExtra("endDate") ?: ""
            )

            viewModel.addTrip(this, trip)
        }
    }

    private fun showDeleteDialog(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("여행 삭제")
            .setMessage("이 여행을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                viewModel.removeTrip(this, position)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun updateEmptyView(list: List<Trip>) {
        emptyLayout.visibility =
            if (list.isEmpty()) View.VISIBLE else View.GONE
    }
}