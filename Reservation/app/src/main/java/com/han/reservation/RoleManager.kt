package com.han.reservation

import com.google.firebase.database.FirebaseDatabase

object RoleManager {

    fun checkAdmin(
        uid: String,
        onResult: (Boolean) -> Unit
    ) {
        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid)
            .child("role")
            .get()
            .addOnSuccessListener { snapshot ->
                val role = snapshot.getValue(String::class.java)
                onResult(role == UserRole.ADMIN.name)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }
}