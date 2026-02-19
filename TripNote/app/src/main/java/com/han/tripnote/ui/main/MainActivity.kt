package com.han.tripnote.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.widget.ArrayAdapter
import com.han.tripnote.data.model.TripStatus
import androidx.recyclerview.widget.LinearLayoutManager
import com.han.tripnote.data.model.Trip
import com.han.tripnote.ui.add.AddTripActivity
import androidx.lifecycle.ViewModelProvider
import com.han.tripnote.data.repository.TripRepository
import com.han.tripnote.databinding.ActivityMainBinding
import com.han.tripnote.ui.detail.TripDetailActivity
import com.han.tripnote.ui.trip.TripFilter
import com.han.tripnote.ui.viewmodel.TripViewModel
import com.han.tripnote.ui.trip.TripSort
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: TripViewModel
    private lateinit var adapter: TripAdapter
    private var selectedFilter: String = "ALL"

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

        val filterItems = listOf(
            "전체",
            "예정",
            "진행중",
            "완료"
        )

        val dropdownAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            filterItems
        )

        binding.dropdownFilter.setAdapter(dropdownAdapter)
        binding.dropdownFilter.setText(filterItems[0], false)

        binding.dropdownFilter.setOnItemClickListener { _, _, position, _ ->

            when (position) {
                0 -> viewModel.setFilter(TripFilter.ALL)
                1 -> viewModel.setFilter(
                    TripFilter.BY_STATUS(TripStatus.UPCOMING)
                )
                2 -> viewModel.setFilter(
                    TripFilter.BY_STATUS(TripStatus.ONGOING)
                )
                3 -> viewModel.setFilter(
                    TripFilter.BY_STATUS(TripStatus.COMPLETED)
                )
            }
        }

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

        viewModel.upcomingCount.observe(this) {
            binding.tvUpcomingCount.text = it.toString()
        }

        viewModel.ongoingCount.observe(this) {
            binding.tvOngoingCount.text = it.toString()
        }

        viewModel.completedCount.observe(this) {
            binding.tvCompletedCount.text = it.toString()
        }

        binding.cardUpcoming.setOnClickListener {
            viewModel.setFilter(TripFilter.BY_STATUS(TripStatus.UPCOMING))
        }

        binding.cardOngoing.setOnClickListener {
            viewModel.setFilter(TripFilter.BY_STATUS(TripStatus.ONGOING))
        }

        binding.cardCompleted.setOnClickListener {
            viewModel.setFilter(TripFilter.BY_STATUS(TripStatus.COMPLETED))
        }

    }

    private fun updateEmptyView(list: List<Trip>) {
        binding.layoutEmpty.visibility =
            if (list.isEmpty()) View.VISIBLE else View.GONE
    }


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