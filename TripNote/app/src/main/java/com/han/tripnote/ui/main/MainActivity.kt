package com.han.tripnote.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.han.tripnote.R
import com.han.tripnote.ui.trip.TripSort

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

        viewModel.filter.observe(this) { filter ->
            updateFilterUI(filter)
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

    /*private fun updateFilterUI(filter: TripFilter) {

        val buttons = listOf(
            binding.btnAll,
            binding.btnUpcoming,
            binding.btnOngoing,
            binding.btnCompleted
        )

        buttons.forEach {
            it.setBackgroundResource(R.drawable.bg_filter_unselected)
            it.setTextColor(
                ContextCompat.getColor(this, android.R.color.black)
            )
        }

        val selectedButton = when (filter) {
            is TripFilter.ALL -> binding.btnAll
            is TripFilter.BY_STATUS -> when (filter.status) {
                TripStatus.UPCOMING -> binding.btnUpcoming
                TripStatus.ONGOING -> binding.btnOngoing
                TripStatus.COMPLETED -> binding.btnCompleted
            }
        }

        selectedButton.setBackgroundResource(R.drawable.bg_filter_selected)
        selectedButton.setTextColor(
            ContextCompat.getColor(this, android.R.color.white)
        )
    }*/

   /* private fun updateSortUI(sort: TripSort) {

        val buttons = listOf(
            binding.btnSortNewest,
            binding.btnSortOldest,
            binding.btnSortStartAsc,
            binding.btnSortStartDesc
        )

        buttons.forEach {
            it.setBackgroundResource(R.drawable.bg_filter_unselected)
            it.setTextColor(
                ContextCompat.getColor(this, android.R.color.black)
            )
        }

        val selectedButton = when (sort) {
            is TripSort.NEWEST -> binding.btnSortNewest
            is TripSort.OLDEST -> binding.btnSortOldest
            is TripSort.START_DATE_ASC -> binding.btnSortStartAsc
            is TripSort.START_DATE_DESC -> binding.btnSortStartDesc
        }

        selectedButton.setBackgroundResource(R.drawable.bg_filter_selected)
        selectedButton.setTextColor(
            ContextCompat.getColor(this, android.R.color.white)
        )
    }*/

    private fun setupFilterChips() {

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

    private fun setupFab() {
        binding.fabAddTrip.setOnClickListener {
            startActivity(Intent(this, AddTripActivity::class.java))
        }
    }

    private fun updateFilterUI(filter: TripFilter) {
        when (filter) {
            is TripFilter.ALL -> binding.btnAll.isChecked = true
            is TripFilter.BY_STATUS -> when (filter.status) {
                TripStatus.UPCOMING -> binding.btnUpcoming.isChecked = true
                TripStatus.ONGOING -> binding.btnOngoing.isChecked = true
                TripStatus.COMPLETED -> binding.btnCompleted.isChecked = true
            }
        }
    }

    private fun updateSortUI(sort: TripSort) {
        when (sort) {
            is TripSort.NEWEST -> binding.btnSortNewest.isChecked = true
            is TripSort.OLDEST -> binding.btnSortOldest.isChecked = true
            is TripSort.START_DATE_ASC -> binding.btnSortStartAsc.isChecked = true
            is TripSort.START_DATE_DESC -> binding.btnSortStartDesc.isChecked = true
        }
    }

}