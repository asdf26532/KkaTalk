package com.han.reservation

import android.util.Log
import com.google.firebase.database.FirebaseDatabase


class FirebaseRepository {
    private val db = FirebaseDatabase.getInstance().reference

    // 가이드 생성
    fun createGuide(guide: Guide, onResult: (Boolean, String?) -> Unit) {
        val id = db.child("guides").push().key ?: return onResult(false, "key 생성 실패")
        guide.id = id
        db.child("guides").child(id).setValue(guide)
            .addOnSuccessListener {
                Log.d("FirebaseRepo", "가이드 생성 성공: $id")
                onResult(true, id)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseRepo", "가이드 생성 실패", e)
                onResult(false, e.message)
            }
    }

    // 가이드 목록 조회
    fun fetchGuides(onResult: (List<Guide>) -> Unit) {
        db.child("guides").get()
            .addOnSuccessListener { snapshot ->
                val list = mutableListOf<Guide>()
                for (child in snapshot.children) {
                    val g = child.getValue(Guide::class.java)
                    if (g != null) list.add(g)
                }
                onResult(list)
            }
            .addOnFailureListener {
                Log.e("FirebaseRepo", "가이드 조회 실패: ${it.message}")
                onResult(emptyList())
            }
    }

    // 예약 생성
    fun createReservation(reservation: Reservation, onResult: (Boolean, String?) -> Unit) {
        val id = db.child("reservations").push().key ?: return onResult(false, "key 생성 실패")
        reservation.id = id
        db.child("reservations").child(id).setValue(reservation)
            .addOnSuccessListener {
                Log.d("FirebaseRepo", "예약 생성 성공: $id")
                onResult(true, id)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseRepo", "예약 생성 실패", e)
                onResult(false, e.message)
            }
    }

    // 예약 목록 조회
    fun fetchReservations(onResult: (List<Reservation>) -> Unit) {
        db.child("reservations").get()
            .addOnSuccessListener { snapshot ->
                val list = mutableListOf<Reservation>()
                for (child in snapshot.children) {
                    val r = child.getValue(Reservation::class.java)
                    if (r != null) list.add(r)
                }
                onResult(list)
            }
            .addOnFailureListener {
                Log.e("FirebaseRepo", "예약 조회 실패: ${it.message}")
                onResult(emptyList())
            }
    }
}
