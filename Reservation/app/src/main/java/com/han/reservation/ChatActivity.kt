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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
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
        //fetchBlockedUsers()

        // 액션바에 상대방 이름 보이기
        supportActionBar?.title = receiverNick
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 화살표 버튼 추가

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