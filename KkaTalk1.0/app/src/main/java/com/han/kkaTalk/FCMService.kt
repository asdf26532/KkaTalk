package com.han.kkaTalk

import android.util.Log
import com.google.firebase.database.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object FCMService {
    private const val FCM_SERVER_KEY = "BHTSL3TG8wIWmH39Q7zL9dXJuUSCo3iRIr8CWBP04-Qlj0VosZuhsperQR_dYhgq9ODrpSSHKkks7zqLWDbyE4w"  // 🔹 FCM 서버 키 (Firebase 콘솔에서 확인)
    private const val FCM_API_URL = "https://fcm.googleapis.com/v1/projects/YOUR_PROJECT_ID/messages:send"

    // ✅ 1. FCM 토큰을 가져와서 푸시 알림 전송
    fun sendNotification(receiverUid: String, message: String) {
        val databaseRef = FirebaseDatabase.getInstance().reference.child("user").child(receiverUid).child("fcmToken")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val token = snapshot.getValue(String::class.java)
                if (!token.isNullOrEmpty()) {

                } else {
                    Log.e("FCMService", "❌ FCM 토큰이 없음")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FCMService", "❌ FCM 토큰 조회 실패: ${error.message}")
            }
        })
    }



}