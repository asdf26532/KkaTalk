package com.han.kkaTalk

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.PopupMenu
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

    private val blockedUserIds: ArrayList<String> = ArrayList()

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
        val messageAdapter = MessageAdapter(this, messageList, profileImageUrl, receiverNick, receiverUid)

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

        // 차단된 사용자 목록 가져오기
        fetchBlockedUsers()

        // 액션바에 상대방 이름 보이기
        supportActionBar?.title = receiverNick
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 화살표 버튼 추가

        binding.btnSend.setOnClickListener {

            if (blockedUserIds.contains(receiverUid)) {
                Toast.makeText(this, "차단한 사용자에게 메시지를 보낼 수 없습니다.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

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
                        // 차단된 사용자의 메시지는 필터링
                        if (blockedUserIds.contains(message.sendId)) {
                            Log.d("ChatActivity", "차단된 사용자의 메시지 필터링: ${message.sendId}")
                            return // 차단된 사용자 메시지 무시
                        }

                        if (message.deleted == true) {
                            message.message = "삭제된 메시지입니다."

                        }

                        messageList.add(message)
                        binding.rvChat.post {
                            messageAdapter.notifyDataSetChanged()
                            binding.rvChat.scrollToPosition(messageList.size - 1)
                        }


                    }

                }
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                    val removedMessage = snapshot.getValue(Message::class.java)
                    if (removedMessage != null) {
                        val index = messageList.indexOfFirst { it.timestamp == removedMessage.timestamp }
                        if (index != -1) {
                            messageList[index].message = "삭제된 메시지입니다."
                            messageList[index].deleted = true

                            // 새로고침
                            runOnUiThread {
                                binding.rvChat.post {
                                    messageAdapter.notifyItemChanged(index)
                                }
                            }
                        }
                    }

                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    // 삭제된 메시지 처리
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    // 이동된 메시지 처리
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DatabaseError", "Database error: $error")
                }
            })

    }

     fun showReactionPopup(message: Message) {
         val reactions = listOf("❤️", "😂", "👍", "😮", "😢", "👎") // 리액션 목록
         val userId = FirebaseAuth.getInstance().currentUser?.uid
         if (userId == null) {
             Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
             return
         }

         // LongClick 발생한 뷰를 기준으로 PopupMenu를 띄우기 위해 itemView를 사용
         val popup = PopupMenu(this, findViewById(android.R.id.content)) // 대체로 안전한 기본 뷰 사용
         reactions.forEach { reaction ->
             popup.menu.add(reaction) // 리액션 목록 추가
         }

         popup.setOnMenuItemClickListener { menuItem ->
             val selectedReaction = menuItem.title.toString()

             // Firebase Database 참조 설정
             val messageRef = FirebaseDatabase.getInstance().getReference("messages/${message.message}")
             val currentReactions =
                 (message.reactions ?: hashMapOf()).toMutableMap() // reactions 초기화

             // 새로운 리액션 추가 (이미 있으면 덮어쓰기)
             currentReactions[userId] = selectedReaction

             // 업데이트된 리액션을 Firebase에 저장
             messageRef.child("reactions").setValue(currentReactions)
                 .addOnSuccessListener {
                     Toast.makeText(this, "리액션이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                 }
                 .addOnFailureListener {
                     Toast.makeText(this, "리액션 추가 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                 }

             true
         }
         popup.show()
    }

     fun showDeletePopup(message: Message) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("메시지 삭제")
        builder.setMessage("이 메시지를 삭제하시겠습니까?")
        builder.setPositiveButton("삭제") { dialog, _ ->
            val senderRoom = FirebaseAuth.getInstance().currentUser?.uid + receiverUid
            val receiverRoom = receiverUid + FirebaseAuth.getInstance().currentUser?.uid

            // 현재 사용자가 메시지를 보낸 사람인지 확인
            if (message.sendId == FirebaseAuth.getInstance().currentUser?.uid) {
                    deleteMessage(senderRoom, receiverRoom, message)
            } else {
                    Toast.makeText(this, "자신이 보낸 메시지만 삭제할 수 있습니다.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
                }
            builder.setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun deleteMessage(senderRoom: String, receiverRoom: String, message: Message) {
        val senderMessagesRef = mDbRef.child("chats").child(senderRoom).child("message")
        val receiverMessagesRef = mDbRef.child("chats").child(receiverRoom).child("message")

        // Sender Room에서 메시지 삭제
        message.timestamp?.toDouble()?.let {
            senderMessagesRef.orderByChild("timestamp")
                .equalTo(it)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (messageSnapshot in snapshot.children) {
                            messageSnapshot.ref.child("deleted").setValue(true)
                                .addOnSuccessListener {
                                    Log.d("deleteMessageInRooms", "SenderRoom: 메시지가 성공적으로 삭제되었습니다.")
                                }
                                .addOnFailureListener { error ->
                                    Log.e("deleteMessageInRooms", "SenderRoom: 메시지 삭제 실패 - ${error.message}")
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("deleteMessageInRooms", "SenderRoom: 데이터베이스 오류 - ${error.message}")
                    }
                })
        }

        // Receiver Room에서 메시지 삭제
        message.timestamp?.toDouble()?.let {
            receiverMessagesRef.orderByChild("timestamp")
                .equalTo(it)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (messageSnapshot in snapshot.children) {
                            messageSnapshot.ref.child("deleted").setValue(true)
                                .addOnSuccessListener {
                                    Log.d("deleteMessageInRooms", "ReceiverRoom: 메시지가 성공적으로 삭제되었습니다.")
                                }
                                .addOnFailureListener { error ->
                                    Log.e("deleteMessageInRooms", "ReceiverRoom: 메시지 삭제 실패 - ${error.message}")
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("deleteMessageInRooms", "ReceiverRoom: 데이터베이스 오류 - ${error.message}")
                    }
                })
        }
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
                        if (blockedUserIds.contains(message.sendId)) {
                            Log.d("markMessagesAsRead", "차단된 사용자의 메시지 읽음 처리 제외: ${message.sendId}")
                            continue // 차단된 사용자의 메시지는 읽음 처리하지 않음
                        }

                        // 읽음 처리
                        if (message.sendId != FirebaseAuth.getInstance().currentUser?.uid) {
                            messageSnapshot.ref.child("mread").setValue(true)
                        }

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

                // 차단 성공 시 결과 전달
                val intent = Intent()
                intent.putExtra("refreshRequired", true) // 새로고침 필요 플래그
                setResult(Activity.RESULT_OK, intent)
                // 차단 후 채팅방 종료
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "차단에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "로그인 상태를 확인하세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchBlockedUsers() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            val userRef = FirebaseDatabase.getInstance().reference.child("user").child(currentUserId).child("blockedUsers")

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    blockedUserIds.clear() // 기존 데이터를 초기화
                    for (child in snapshot.children) {
                        val blockedUserId = child.key
                        if (blockedUserId != null) {
                            blockedUserIds.add(blockedUserId)
                        }
                    }
                    Log.d("ChatActivity", "Blocked Users: $blockedUserIds") // 차단된 사용자 확인
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatActivity", "Failed to fetch blocked users: ${error.message}")
                }
            })
        }
    }

    private fun addReactionToMessage(messageId: String, reaction: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val messageRef = mDbRef.child("chats").child(receiverRoom).child("message").child(messageId)

        messageRef.child("reactions").child(currentUserId).setValue(reaction).addOnSuccessListener {
            Log.d("ChatActivity", "Reaction added successfully")
        }.addOnFailureListener {
            Log.e("ChatActivity", "Failed to add reaction: ${it.message}")
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
                    AlertDialog.Builder(this)
                    .setTitle("사용자 차단")
                    .setMessage("대화 상대를 차단하시겠습니까?")
                    .setPositiveButton("차단하기") { dialog, _ ->
                        blockUser(receiverUid) // 차단 실행
                        dialog.dismiss()
                    }
                    .setNegativeButton("취소") { dialog, _ ->
                        dialog.dismiss() // 다이얼로그 닫기
                    }
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}