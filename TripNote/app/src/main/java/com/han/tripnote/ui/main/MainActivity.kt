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
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import com.han.tripnote.ui.detail.TripDetailActivity
import com.han.tripnote.ui.viewmodel.TripViewModel

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
            onItemLongClick = { trip ->
                viewModel.removeTrip(trip)
            }
        )

        recyclerView.adapter = adapter

        emptyLayout = findViewById(R.id.layoutEmpty)

        viewModel.tripList.observe(this) { list ->
            adapter.submitList(list)
            updateEmptyView(list)
        }


        // 여행 추가 버튼
        findViewById<View>(R.id.fabAddTrip).setOnClickListener {
            startActivity(Intent(this, AddTripActivity::class.java))
        }
    }

    private fun updateEmptyView(list: List<Trip>) {
        emptyLayout.visibility =
            if (list.isEmpty()) View.VISIBLE else View.GONE
    }
}