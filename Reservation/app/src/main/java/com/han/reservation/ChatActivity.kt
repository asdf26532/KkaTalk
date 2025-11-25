package com.han.reservation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.han.reservation.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private lateinit var messageAdapter: MessageAdapter

    private lateinit var senderUid: String
    private lateinit var receiverNick: String
    private lateinit var receiverUid: String

    private lateinit var binding: ActivityChatBinding

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var prefs: SharedPreferences

    private lateinit var receiverRoom: String
    private lateinit var senderRoom: String

    private var profileImageUrl: String? = null
    private lateinit var messageList: ArrayList<Message>

    private val blockedUserIds: ArrayList<String> = ArrayList()
    private var blockTimeStamp: Long = Long.MAX_VALUE

    private var originalList: List<Message>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 넘어온 데이터 변수에 담기
        receiverNick = intent.getStringExtra("nick").toString()
        receiverUid = intent.getStringExtra("uId").toString()
        profileImageUrl = intent.getStringExtra("profileImageUrl") ?: ""

        // 데이터가 제대로 넘어오는지 로그 확인
        Log.d("ChatActivity", "Receiver: $receiverNick ($receiverUid), Profile: $profileImageUrl")

        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList, profileImageUrl, receiverNick, receiverUid)

        // RecyclerView
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter = messageAdapter

        binding.edtMessage.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.rvChat.postDelayed({
                    (binding.rvChat.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                        messageList.size - 1, 0
                    )
                }, 200)
            }
        }

        // Firebase 설정
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference
        prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // 접속자 Uid
        senderUid = mAuth.currentUser?.uid
            ?: prefs.getString("userId", null).orEmpty()

        if (senderUid.isEmpty()) {
            Log.e("ChatActivity", "현재 사용자 ID를 찾을 수 없습니다.")
            finish()
            return
        }

        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid

        // 차단된 사용자 목록 가져오기
        fetchBlockedUsers()

        // 액션바에 상대방 이름 보이기
        supportActionBar?.title = receiverNick
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 화살표 버튼 추가

        binding.btnSend.setOnClickListener {
            val message = binding.edtMessage.text.toString().trim()
            // 메시지가 비어 있는지 확인
            if (message.isEmpty()) {
                showCustomToast("내용을 입력하세요.")
                return@setOnClickListener
            }

            if (blockedUserIds.contains(receiverUid)) {
                showCustomToast("차단한 사용자에게 메시지를 보낼 수 없습니다.")
                return@setOnClickListener
            }

            val timeStamp = System.currentTimeMillis()
            val mread = false

            val messageObject = Message(message, senderUid, receiverUid, timeStamp, mread)

            // Firebase에서 차단 여부 확인
            mDbRef.child("user").child(receiverUid).child("blockedUsers")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("ChatActivity", "차단 리스트 불러옴: ${snapshot.value}")

                        //  blockedUsers를 Map<String, Map<String, Long>> 형태로 가져오기
                        val blockedUsersMap = snapshot.getValue(object : GenericTypeIndicator<Map<String, Map<String, Long>>>() {}) ?: emptyMap()

                        Log.d("ChatActivity", " 차단된 유저 목록: $blockedUsersMap")

                        // 차단된 유저 ID만 리스트로 변환
                        val blockedUserIds = blockedUsersMap.keys.toList()
                        Log.d("ChatActivity", " 차단된 유저 ID 리스트: $blockedUserIds")

                        val isBlocked = blockedUserIds.contains(senderUid)
                        Log.d("ChatActivity", " 차단 여부 확인: $senderUid → ${if (isBlocked) "차단됨" else "차단 안 됨"}")
                        if (isBlocked) {
                            Log.d("ChatActivity", "유저 $senderUid 는 차단당했음! receiverRoom에 메시지 저장 안 함.")

                            // 차단당한 유저의 메시지는 receiverRoom에 저장되지 않음
                            mDbRef.child("chats").child(senderRoom).child("message").push()
                                .setValue(messageObject)
                        } else {
                            Log.d("ChatActivity", " 정상 저장됨.")
                            mDbRef.child("chats").child(senderRoom).child("message").push()
                                .setValue(messageObject).addOnSuccessListener {
                                    mDbRef.child("chats").child(receiverRoom).child("message").push()
                                        .setValue(messageObject)
                                        .addOnSuccessListener {

                                        }
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ChatActivity", "차단 여부 확인 중 에러 발생: $error")
                    }
                })


            // 입력 부분 초기화
            binding.edtMessage.setText("")
        }

        Log.d("ChatActivity", "Sender UID: $senderUid")

        // 메시지 가져오기
        mDbRef.child("chats").child(senderRoom).child("message")
            .addChildEventListener(object : ChildEventListener {

                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = snapshot.getValue(Message::class.java)

                    if (message != null) {
                        val messageTime = message.timestamp ?: 0

                        // 차단된 사용자에게서 온 메시지라면 필터링
                        if (blockedUserIds.contains(message.sendId) && messageTime > blockTimeStamp) {
                            Log.d("ChatActivity", "차단된 사용자의 메시지 필터링: ${message.sendId}")
                            return  // 차단된 사용자의 메시지는 추가하지 않음
                        }

                        // 차단된 메시지가 아니면 기존 메시지 처리
                        if (message.deleted == true) {
                            message.message = "삭제된 메시지입니다."
                        }
                        messageList.add(message)

                        // RecyclerView 갱신
                        binding.rvChat.post {
                            messageAdapter.notifyDataSetChanged()
                            binding.rvChat.scrollToPosition(messageList.size - 1)
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    // 변경된 메시지 처리
                    val changedMessage = snapshot.getValue(Message::class.java)
                    if (changedMessage != null) {
                        val index = messageList.indexOfFirst { it.timestamp == changedMessage.timestamp }
                        if (index != -1) {
                            if (changedMessage.deleted == true && messageList[index].deleted != true) {
                                messageList[index].message = "삭제된 메시지입니다."
                                messageList[index].deleted = true

                                // 새로고침
                                runOnUiThread {
                                    binding.rvChat.post {
                                        messageAdapter.notifyItemChanged(index)
                                    }
                                }
                            } else if (changedMessage.reactions != messageList[index].reactions) {
                                messageList[index].reactions = changedMessage.reactions
                                runOnUiThread {
                                    binding.rvChat.post {
                                        messageAdapter.notifyItemChanged(index)
                                    }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SELECT_FILE && resultCode == Activity.RESULT_OK) {
            // 선택된 파일의 URI 가져오기
            val fileUri = data?.data
            if (fileUri != null) {
                uploadFileToFirebase(fileUri)
            } else {
                showCustomToast("파일 선택 실패")
            }
        }
    }

    // Firebase Storage에 파일 업로드
    private fun uploadFileToFirebase(fileUri: Uri) {
        val storageReference = FirebaseStorage.getInstance().reference.child("chat_files/${System.currentTimeMillis()}")
        val uploadTask = storageReference.putFile(fileUri)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                val fileUrl = uri.toString()

                // 파일 URL로 메시지 전송
                sendMessageWithFile(fileUrl)
            }
        }.addOnFailureListener {
            showCustomToast("파일 업로드 실패")
        }
    }

    // 파일 메시지 전송 함수
    private fun sendMessageWithFile(fileUrl: String) {
        val timeStamp = System.currentTimeMillis()
        val messageObject = Message(
            message = null,         // 텍스트 메시지는 없음
            sendId = senderUid,
            receiverId = receiverUid,
            timestamp = timeStamp,
            fileUrl = fileUrl,      // 업로드된 파일 URL
            mread = false
        )

        Log.d("ChatActivity", "sendMessageWithFile: Sender UID: $senderUid")

        mDbRef.child("chats").child(senderRoom).child("message").push()
            .setValue(messageObject).addOnSuccessListener {
                mDbRef.child("chats").child(receiverRoom).child("message").push()
                    .setValue(messageObject)
            }
    }

    companion object {
        const val REQUEST_CODE_SELECT_FILE = 401 // 파일 선택 요청 코드
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(messageList)
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = messageAdapter
        }
    }

    private fun setupSendButton() {
        binding.btnSend.setOnClickListener {
            val msg = binding.editMessage.text.toString().trim()

            if (msg.isEmpty()) return@setOnClickListener

            // 메시지 객체 생성
            val newMessage = Message(
                messageId = System.currentTimeMillis().toString(), // 임시 ID
                senderId = "local_user", // Firebase는 3일차에 붙임
                content = msg,
                timestamp = System.currentTimeMillis()
            )

            // 리스트에 추가
            messageList.add(newMessage)

            // 화면 갱신
            messageAdapter.notifyItemInserted(messageList.size - 1)

            // 스크롤 맨 아래로 이동
            binding.recyclerViewChat.scrollToPosition(messageList.size - 1)

            // 입력창 초기화
            binding.editMessage.setText("")
        }
    }
}