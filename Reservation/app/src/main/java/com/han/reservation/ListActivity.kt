package com.han.reservation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ListActivity : AppCompatActivity() {


    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReservationAdapter
    private val repo = FirebaseRepository()

    private val database = FirebaseDatabase.getInstance().reference
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        recyclerView = findViewById(R.id.recyclerViewReservations)
        adapter = ReservationAdapter { reservationId ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("reservationId", reservationId)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            userId = currentUser.uid
            loadReservations()
        } else {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
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
                            it.id = child.key ?: ""   // Reservation 클래스에 id 필드 있어야 함
                            reservations.add(it)
                        }
                    }
                    adapter.submitList(reservations)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ListActivity", "예약 불러오기 실패: ${error.message}")
                }
            })
    }
}