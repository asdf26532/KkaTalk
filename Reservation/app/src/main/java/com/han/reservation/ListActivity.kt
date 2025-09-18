package com.han.reservation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReservationAdapter
    private val repo = FirebaseRepository()

    // 로그인된 유저 ID (임시)
    private val currentUserId = "USER_001"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        adapter = ReservationAdapter { reservationId ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("reservationId", reservationId)
            startActivity(intent)
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            userId = currentUser.uid
            loadReservations()
        }
    }

    private fun loadReservations() {
        database.child("reservations")
            .orderByChild("userId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val reservations = mutableListOf<Reservation>()
                    for (child in snapshot.children) {
                        val reservation = child.getValue(Reservation::class.java)
                        reservation?.let {
                            it.reservationId = child.key ?: ""
                            reservations.add(it)
                        }
                    }
                    adapter.submitList(reservations)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}