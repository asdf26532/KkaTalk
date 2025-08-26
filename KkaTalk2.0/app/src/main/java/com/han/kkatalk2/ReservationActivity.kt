package com.han.kkatalk2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class ReservationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference
    private lateinit var reservationAdapter: ReservationAdapter
    private lateinit var reservationList: MutableList<Reservation>

    private var chatRoomId: String? = null
    private var currentUid: String = ""
    private var userRole: String = "user"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference

        // 현재 채팅방 id
        chatRoomId = intent.getStringExtra("chatRoomId")

        // 현재 유저 UID
        val currentUser = auth.currentUser
        if (currentUser != null) {
            currentUid = currentUser.uid
        }

        // RecyclerView
        reservationList = mutableListOf()
        reservationAdapter = ReservationAdapter(reservationList, userRole)
        val recyclerView = findViewById<RecyclerView>(R.id.reservationRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = reservationAdapter

        // FloatingActionButton (가이드만 보이게)
        val addFab = findViewById<FloatingActionButton>(R.id.fab_add_reservation)

        // role 확인
        checkUserRole { role ->
            userRole = role
            if (role == "guide") {
                addFab.show()
                addFab.setOnClickListener {
                    // 예약 추가 다이얼로그 열기
                    showAddReservationDialog()
                }
            } else {
                addFab.hide()
            }
        }

        // 예약 불러오기
        loadReservations()
    }

    private fun checkUserRole(callback: (String) -> Unit) {
        db.child("users").child(currentUid).child("role")
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val role = snapshot.getValue(String::class.java) ?: "user"
                    callback(role)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadReservations() {
        chatRoomId?.let { roomId ->
            db.child("reservations").child(roomId)
                .addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        reservationList.clear()
                        for (child in snapshot.children) {
                            val reservation = child.getValue(Reservation::class.java)
                            reservation?.let { reservationList.add(it) }
                        }
                        reservationAdapter.notifyDataSetChanged()
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@ReservationActivity, "불러오기 실패", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun showAddReservationDialog() {
        // 날짜 선택 다이얼로그 + Firebase 저장 로직 추가 예정
    }

}