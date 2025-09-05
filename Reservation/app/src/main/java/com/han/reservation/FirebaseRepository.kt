package com.han.reservation

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseRepository {
    private val db = Firebase.database.reference

    // 가이드 목록 읽기
    fun fetchGuides(onComplete: (List<Guide>) -> Unit, onError: (Exception) -> Unit) {
        db.child("guides").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Guide>()
                snapshot.children.forEach {
                    val g = it.getValue(Guide::class.java)
                    g?.let { list.add(it) }
                }
                onComplete(list)
            }
            override fun onCancelled(error: DatabaseError) { onError(error.toException()) }
        })
    }

    // 예약 생성
    fun createReservation(res: Reservation, callback: (Boolean, String?) -> Unit) {
        val ref = db.child("reservations").push()
        res.id = ref.key ?: ""
        ref.setValue(res)
            .addOnSuccessListener { callback(true, res.id) }
            .addOnFailureListener { callback(false, it.message) }
    }

    // 사용자 예약 조회
    fun fetchReservationsByUser(userId: String, onComplete: (List<Reservation>) -> Unit, onError: (Exception) -> Unit) {
        db.child("reservations").orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Reservation>()
                    snapshot.children.forEach {
                        val r = it.getValue(Reservation::class.java)
                        r?.let { list.add(it) }
                    }
                    onComplete(list)
                }
                override fun onCancelled(error: DatabaseError) { onError(error.toException()) }
            })
    }

    // 예약 상태 업데이트(취소 등)
    fun updateReservationStatus(resId: String, newStatus: String, callback:(Boolean)->Unit) {
        db.child("reservations").child(resId).child("status").setValue(newStatus)
            .addOnSuccessListener { callback(true) }.addOnFailureListener { callback(false) }
    }
}
