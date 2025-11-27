package com.han.reservation

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        // FloatingActionButton 바인딩
        binding.btnScrollToBottom.setOnClickListener {
            if (messageList.isNotEmpty()) {
                binding.rvChat.scrollToPosition(messageList.size - 1) // 가장 마지막 메시지로 스크롤
            }
        }

        // RecyclerView 스크롤 리스너 추가
        binding.rvChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // 대화가 없으면 버튼 숨김
                if (messageList.isEmpty()) {
                    binding.btnScrollToBottom.hide()
                    return // 추가 작업 없이 종료
                }

                // 스크롤 상태에 따라 버튼 표시/숨김
                val layoutManager = binding.rvChat.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (lastVisibleItemPosition < messageList.size - 1) {
                    // 마지막 메시지가 화면에 보이지 않으면 버튼 표시
                    binding.btnScrollToBottom.show()
                } else {
                    // 마지막 메시지가 화면에 보이면 버튼 숨김
                    binding.btnScrollToBottom.hide()
                }
            }
        })

        binding.btnAttach.setOnClickListener {
            // Intent를 사용해 파일 선택
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*" // 모든 파일 타입을 허용
            startActivityForResult(intent, REQUEST_CODE_SELECT_FILE)
        }

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

    // 메세지 전달 기능
    private fun shareMessage(message: Message) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain" // 텍스트 데이터 유형
            putExtra(Intent.EXTRA_TEXT, message.message) // Message 객체에서 메시지 내용만 추출
        }
        // 공유 가능한 앱 목록 보여주기
        val chooser = Intent.createChooser(intent, "메시지 전달")
        startActivity(chooser)
    }

    private fun updateReactions(messagesRef: DatabaseReference, message: Message, userId: String, reaction: String) {
        message.timestamp?.toDouble()?.let { timestamp ->
            messagesRef.orderByChild("timestamp")
                .equalTo(timestamp)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (messageSnapshot in snapshot.children) {
                            val currentReactions = (messageSnapshot.child("reactions").value as? HashMap<String, String>)
                                ?: hashMapOf()

                            // 리액션 추가
                            currentReactions[userId] = reaction

                            // 업데이트된 리액션을 Firebase에 저장
                            messageSnapshot.ref.child("reactions").setValue(currentReactions)
                                .addOnSuccessListener {
                                    Log.d("updateReactions", "리액션이 성공적으로 추가되었습니다.")
                                }
                                .addOnFailureListener { error ->
                                    Log.e("updateReactions", "리액션 추가 실패: ${error.message}")
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("updateReactions", "데이터베이스 오류: ${error.message}")
                    }
                })
        }
    }

    // 보낸 메세지 팝업 옵션
    fun showOptionsPopup(message: Message) {
        val dialogView = layoutInflater.inflate(R.layout.message_options, null)

        val btnCopy = dialogView.findViewById<TextView>(R.id.btn_copy)
        val btnDelete = dialogView.findViewById<TextView>(R.id.btn_delete)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btn_cancel)

        // AlertDialog 생성
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.show()

        // 복사 버튼 클릭
        btnCopy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Message", message.message)
            clipboard.setPrimaryClip(clip)
            showCustomToast("메시지가 복사되었습니다.")
            dialog.dismiss()
        }

        // 삭제 버튼 클릭
        btnDelete.setOnClickListener {
            showDeletePopup(message) // 기존 삭제 다이얼로그 호출
            dialog.dismiss()
        }

        // 취소 버튼 클릭
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
    }

    // 메세지 삭제 팝업
    private fun showDeletePopup(message: Message) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("메시지 삭제")
        builder.setMessage("이 메시지를 삭제하시겠습니까?")
        builder.setPositiveButton("삭제") { dialog, _ ->

            // 현재 사용자가 메시지를 보낸 사람인지 확인
            if (message.sendId == FirebaseAuth.getInstance().currentUser?.uid) {
                deleteMessage(senderRoom, receiverRoom, message)
            } else {
                showCustomToast("자신이 보낸 메시지만 삭제할 수 있습니다.")
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    // 보낸 메세지 삭제 기능
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
}