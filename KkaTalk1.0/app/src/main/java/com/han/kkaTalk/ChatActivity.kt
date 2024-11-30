package com.han.kkaTalk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.han.kkaTalk.databinding.ActivityChatBinding



class ChatActivity : AppCompatActivity() {

    private lateinit var receiverNick: String
    private lateinit var receiverUid: String

    private lateinit var binding: ActivityChatBinding

    lateinit var mAuth: FirebaseAuth
    lateinit var mDbRef: DatabaseReference

    private lateinit var receiverRoom: String
    private lateinit var senderRoom: String

    private var profileImageUrl: String? = null
    private lateinit var messageList: ArrayList<Message>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 넘어온 데이터 변수에 담기
        receiverNick = intent.getStringExtra("nick").toString()
        receiverUid = intent.getStringExtra("uId").toString()
        profileImageUrl = intent.getStringExtra("profileImageUrl") ?: ""

        // 데이터가 제대로 넘어오는지 로그 확인
        Log.d("ChatActivity", "Receiver Name: $receiverNick")
        Log.d("ChatActivity", "Receiver UID: $receiverUid")
        Log.d("ChatActivity", "Profile Image URL: $profileImageUrl")

        messageList = ArrayList()
        val messageAdapter = MessageAdapter(this, messageList, profileImageUrl, receiverNick)

        // RecyclerView
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter = messageAdapter

        // Firebase 설정
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference

        // 접속자 Uid
        val senderUid = mAuth.currentUser?.uid

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid


        // 액션바에 상대방 이름 보이기
        supportActionBar?.title = receiverNick
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 화살표 버튼 추가

        binding.btnSend.setOnClickListener {
            val message = binding.edtMessage.text.toString()
            val timeStamp = System.currentTimeMillis()
            val mread = false

            val messageObject = Message(message, senderUid, receiverUid, timeStamp, mread)

            // 데이터 저장
            mDbRef.child("chats").child(senderRoom).child("message").push()
                .setValue(messageObject).addOnSuccessListener {
                    // 저장 성공시
                    mDbRef.child("chats").child(receiverRoom).child("message").push()
                        .setValue(messageObject)
                }

            // 입력 부분 초기화
            binding.edtMessage.setText("")
        }

        // 메시지 가져오기
        mDbRef.child("chats").child(senderRoom).child("message")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = snapshot.getValue(Message::class.java)
                    if (message != null) {
                        messageList.add(message)
                        binding.rvChat.post {
                            messageAdapter.notifyDataSetChanged()
                            binding.rvChat.scrollToPosition(messageList.size - 1)
                        }
                    }

                }
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    // 변경된 메시지 처리 (필요 시 구현)
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    // 삭제된 메시지 처리 (필요 시 구현)
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    // 이동된 메시지 처리 (필요 시 구현)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DatabaseError", "Database error: $error")
                }
            })
    }

    private fun markMessagesAsRead(senderRoom: String, receiverRoom: String) {
        val senderMessagesRef = mDbRef.child("chats").child(senderRoom).child("message")
        val receiverMessagesRef = mDbRef.child("chats").child(receiverRoom).child("message")

        // Sender Room 업데이트
        senderMessagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(Message::class.java)
                    if (message != null && (message.mread == false) && message.sendId != FirebaseAuth.getInstance().currentUser?.uid) {
                        // Sender Room 업데이트
                        messageSnapshot.ref.child("mread").setValue(true)

                        // Receiver Room 업데이트
                        message.timestamp?.let { timestamp ->  // timestamp가 null이 아닌 경우만 실행
                            receiverMessagesRef.orderByChild("timestamp")
                                .equalTo(timestamp.toDouble())
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(receiverSnapshot: DataSnapshot) {
                                        for (receiverMessageSnapshot in receiverSnapshot.children) {
                                            receiverMessageSnapshot.ref.child("mread")
                                                .setValue(true)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e("markMessagesAsRead", "Database error: $error")
                                    }
                                })
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("markMessagesAsRead", "Database error: $error")
            }
        })
    }

    private fun blockUser(blockedUserId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            val userRef = FirebaseDatabase.getInstance().reference.child("user").child(currentUserId).child("blockedUsers")
            userRef.child(blockedUserId).setValue(true).addOnSuccessListener {
                Toast.makeText(this, "사용자를 차단했습니다.", Toast.LENGTH_SHORT).show()
                // 차단 후 채팅방 종료
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "차단에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "로그인 상태를 확인하세요.", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onResume() {
        super.onResume()
        // 메시지 읽음 상태 업데이트
        markMessagesAsRead(senderRoom, receiverRoom)
    }

    // 사용자 차단하기 구현
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // 뒤로 가기 버튼 동작 구현
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent()
                intent.putExtra("chatUpdated", true) // 결과 값으로 '갱신 필요' 플래그 전달
                setResult(Activity.RESULT_OK, intent)
                Log.d("ChatActivity", "setResult 호출됨") // 로그 추가
                onBackPressed()
                true
            }

            R.id.menu_block_user -> { // 오른쪽 위 차단하기 버튼
                blockUser(receiverUid)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}