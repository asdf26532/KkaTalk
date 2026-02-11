package com.han.tripnote.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.han.tripnote.data.model.Trip
import com.han.tripnote.ui.add.AddTripActivity
import androidx.lifecycle.ViewModelProvider
import com.han.tripnote.data.model.TripStatus
import com.han.tripnote.data.repository.TripRepository
import com.han.tripnote.databinding.ActivityMainBinding
import com.han.tripnote.ui.detail.TripDetailActivity
import com.han.tripnote.ui.trip.TripFilter
import com.han.tripnote.ui.viewmodel.TripViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: TripViewModel
    private lateinit var adapter: TripAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        TripRepository.init(this)

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

        binding.rvTripList.layoutManager = LinearLayoutManager(this)
        binding.rvTripList.adapter = adapter

        viewModel.filteredTrips.observe(this) { list ->
            adapter.submitList(list)
            updateEmptyView(list)
        }

        binding.fabAddTrip.setOnClickListener {
            startActivity(Intent(this, AddTripActivity::class.java))
        }

        binding.btnAll.setOnClickListener {
            viewModel.setFilter(TripFilter.ALL)
        }

        binding.btnUpcoming.setOnClickListener {
            viewModel.setFilter(
                TripFilter.BY_STATUS(TripStatus.UPCOMING)
            )
        }

        binding.btnOngoing.setOnClickListener {
            viewModel.setFilter(
                TripFilter.BY_STATUS(TripStatus.ONGOING)
            )
        }

        binding.btnCompleted.setOnClickListener {
            viewModel.setFilter(
                TripFilter.BY_STATUS(TripStatus.COMPLETED)
            )
        }
    }

    private fun updateEmptyView(list: List<Trip>) {
        binding.layoutEmpty.visibility =
            if (list.isEmpty()) View.VISIBLE else View.GONE
    }
}