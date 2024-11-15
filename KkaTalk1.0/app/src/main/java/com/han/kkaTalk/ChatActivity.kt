package com.han.kkaTalk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
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
    private lateinit var messageAdapter: MessageAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 넘어온 데이터 변수에 담기
        receiverNick = intent.getStringExtra("nick").toString()
        receiverUid = intent.getStringExtra("uId").toString()
        profileImageUrl = intent.getStringExtra("profileImageUrl").toString()

        // 데이터가 제대로 넘어오는지 로그 확인
        Log.d("ChatActivity", "Receiver Name: $receiverNick")
        Log.d("ChatActivity", "Receiver UID: $receiverUid")
        Log.d("ChatActivity", "Profile Image URL: $profileImageUrl")

        messageList = ArrayList()
        val messageAdapter: MessageAdapter = MessageAdapter(this, messageList, profileImageUrl, receiverNick)

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

        binding.btnSend.setOnClickListener{

            val message = binding.edtMessage.text.toString()
            val timeStamp = System.currentTimeMillis()

            val messageObject = Message(message, senderUid, receiverUid, timeStamp)

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
            .addValueEventListener(object: ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()

                    for(postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        if (message != null) {
                            messageList.add(message)
                        }
                    }
                    // 적용
                    messageAdapter.notifyItemInserted(messageList.size - 1)
                    binding.rvChat.scrollToPosition(messageList.size - 1) // 스크롤을 최신 메시지로 이동

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

    }

    // 메시지 읽음 상태 업데이트 함수
    private fun markMessagesAsRead(chatRoomId: String) {
        val database = FirebaseDatabase.getInstance().reference
        val messagesRef = database.child("chats").child(chatRoomId).child("message")

        messagesRef.get().addOnSuccessListener { snapshot ->
            for (messageSnapshot in snapshot.children) {
                val message = messageSnapshot.getValue(Message::class.java)
                if (message != null && !message.isRead && message.sendId != FirebaseAuth.getInstance().currentUser?.uid) {
                    // 메시지가 읽히지 않았고, 현재 사용자가 보낸 메시지가 아닐 경우 업데이트
                    messageSnapshot.ref.child("isRead").setValue(true)
                    }
                }
            }
        }

    override fun onResume() {
        super.onResume()
        // 메시지 읽음 상태 업데이트
        markMessagesAsRead(senderRoom)
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

            else -> super.onOptionsItemSelected(item)
        }

    }
}