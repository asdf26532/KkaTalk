package com.han.kkatalk2.utls

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

object UserManager {
    private val TAG = "UserManager"

    fun loadUserData(
        context: Context,
        userId: String,
        storage: FirebaseStorage,
        userRef: DatabaseReference,
        onResult: (nick: String, status: String, profileImageUrl: String?) -> Unit,
        imageView: ImageView,
        onDone: () -> Unit,
        defaultProfileResId: Int
    ) {
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nick = snapshot.child("nick").getValue(String::class.java) ?: "닉네임 없음"
                val status = snapshot.child("statusMessage").getValue(String::class.java) ?: "상태 없음"
                val profileUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                onResult(nick, status, profileUrl)

                if (!profileUrl.isNullOrEmpty()) {
                    val storageRef = storage.getReferenceFromUrl(profileUrl)
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        Glide.with(context).load(uri).into(imageView)
                    }.addOnFailureListener {
                        imageView.setImageResource(defaultProfileResId)
                    }.addOnCompleteListener { onDone() }
                } else {
                    imageView.setImageResource(defaultProfileResId)
                    onDone()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_LONG).show()
                onDone()
            }
        })
    }

    fun updateNickname(
        context: Context,
        userRef: DatabaseReference,
        userId: String,
        newNick: String,
        onSuccess: () -> Unit,
        onFail: () -> Unit
    ) {
        if (userId.isNotEmpty()) {
            userRef.child("nick").setValue(newNick)
                .addOnSuccessListener {
                    Toast.makeText(context, "닉네임이 변경되었습니다.", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "닉네임 변경 실패", Toast.LENGTH_SHORT).show()
                    onFail()
                }
        }
    }

    fun uploadProfileImage(
        context: Context,
        uri: Uri,
        userId: String,
        storage: FirebaseStorage,
        userRef: DatabaseReference,
        onSuccess: () -> Unit,
        onFail: () -> Unit
    ) {
        val storageRef = storage.reference.child("profileImages/$userId.jpg")

        storageRef.delete().addOnSuccessListener {
            Log.d(TAG, "기존 이미지 삭제")
        }.addOnFailureListener {
            Log.w(TAG, "기존 이미지 없음 또는 삭제 실패")
        }

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    userRef.child("profileImageUrl").setValue(downloadUri.toString())
                        .addOnSuccessListener {
                            Toast.makeText(context, "프로필 사진 변경 완료", Toast.LENGTH_SHORT).show()
                            onSuccess()
                        }.addOnFailureListener {
                            Toast.makeText(context, "이미지 주소 저장 실패", Toast.LENGTH_SHORT).show()
                            onFail()
                        }
                }
            }.addOnFailureListener {
                Toast.makeText(context, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                onFail()
            }
    }

    fun applyDarkMode(enabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

}
