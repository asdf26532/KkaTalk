package com.han.kkaTalk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
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

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    private lateinit var receiverRoom: String
    private lateinit var senderRoom: String

    private var profileImageUrl: String? = null
    private lateinit var messageList: ArrayList<Message>
    private lateinit var messageAdapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 데이터 가져오기
        receiverNick = intent.getStringExtra("nick").toString()
        receiverUid = intent.getStringExtra("uId").toString()
        profileImageUrl = intent.getStringExtra("profileImageUrl") ?: ""

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference

        val senderUid = mAuth.currentUser?.uid
        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList, profileImageUrl, receiverNick)

        // RecyclerView 초기화
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter = messageAdapter

        // Firebase에서 메시지 로드
        loadMessages()

        // 액션바 설정
        supportActionBar?.title = receiverNick
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 메시지 전송
        binding.btnSend.setOnClickListener {
            sendMessage(senderUid)
        }
    }

    private fun loadMessages() {
        // RecyclerView 데이터 초기화
        messageList.clear()

        mDbRef.child("chats").child(senderRoom).child("message")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = snapshot.getValue(Message::class.java)
                    if (message != null) {
                        messageList.add(message)
                        binding.rvChat.post {
                            // RecyclerView 갱신 및 스크롤
                            messageAdapter.notifyItemInserted(messageList.size - 1)
                            binding.rvChat.scrollToPosition(messageList.size - 1)
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    // 메시지 변경 처리 (필요 시 구현)
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    // 메시지 삭제 처리 (필요 시 구현)
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    // 메시지 이동 처리 (필요 시 구현)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DatabaseError", "Error: ${error.message}")
                }
            })
    }

    private fun sendMessage(senderUid: String?) {
        val message = binding.edtMessage.text.toString()
        val timeStamp = System.currentTimeMillis()
        val mread = false

        val messageObject = Message(message, senderUid, receiverUid, timeStamp, mread)

        // 메시지 저장
        mDbRef.child("chats").child(senderRoom).child("message").push()
            .setValue(messageObject).addOnSuccessListener {
                mDbRef.child("chats").child(receiverRoom).child("message").push()
                    .setValue(messageObject)
            }

        // 입력 필드 초기화
        binding.edtMessage.setText("")
    }

    override fun onResume() {
        super.onResume()
        // 메시지 읽음 처리
        markMessagesAsRead(senderRoom, receiverRoom)
    }

    private fun markMessagesAsRead(senderRoom: String, receiverRoom: String) {
        val senderMessagesRef = mDbRef.child("chats").child(senderRoom).child("message")

        senderMessagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(Message::class.java)
                    if (message != null && message.sendId != mAuth.currentUser?.uid) {
                        messageSnapshot.ref.child("mread").setValue(true)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("markMessagesAsRead", "Error: ${error.message}")
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
