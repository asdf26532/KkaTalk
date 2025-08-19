package com.han.kkatalk2

import com.google.firebase.database.FirebaseDatabase
import com.han.kkatalk2.Booking

class BookingRepository {
    private val database = FirebaseDatabase.getInstance().getReference("bookings")

    fun createBooking(booking: Booking, onComplete: (Boolean) -> Unit) {
        val key = database.push().key
        if (key != null) {
            booking.bookingId = key
            database.child(key).setValue(booking)
                .addOnCompleteListener { task ->
                    onComplete(task.isSuccessful)
                }
        } else {
            onComplete(false)
        }
    }

    fun updateBookingStatus(bookingId: String, status: String, onComplete: (Boolean) -> Unit) {
        database.child(bookingId).child("status").setValue(status)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }
}