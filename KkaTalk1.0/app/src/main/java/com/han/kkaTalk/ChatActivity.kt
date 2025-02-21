package com.han.kkaTalk

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.han.kkaTalk.databinding.ActivityChatBinding



class ChatActivity : AppCompatActivity() {

    private lateinit var messageAdapter: MessageAdapter

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
        Log.d("ChatActivity", "Receiver Name: $receiverNick")
        Log.d("ChatActivity", "Receiver UID: $receiverUid")
        Log.d("ChatActivity", "Profile Image URL: $profileImageUrl")

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

        // 접속자 Uid
        val senderUid = mAuth.currentUser?.uid

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
                Toast.makeText(this, "내용을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (blockedUserIds.contains(receiverUid)) {
                Toast.makeText(this, "차단한 사용자에게 메시지를 보낼 수 없습니다.", Toast.LENGTH_LONG).show()
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
                Toast.makeText(this, "파일 선택 실패", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, "파일 업로드 실패", Toast.LENGTH_SHORT).show()
        }
    }

    // 파일 메시지 전송 함수
    private fun sendMessageWithFile(fileUrl: String) {
        val senderUid = mAuth.currentUser?.uid
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



    fun showReactionPopup(message: Message) {

        val reactions = listOf("❤️", "👍", "👎", "😂", "😮", "😢", "✅") // 리액션 목록
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val senderMessagesRef = mDbRef.child("chats").child(senderRoom).child("message")
        val receiverMessagesRef = mDbRef.child("chats").child(receiverRoom).child("message")


        // 팝업을 위한 커스텀 레이아웃 초기화
        val popupView = layoutInflater.inflate(R.layout.popup_reaction, null)
        val reactionContainer = popupView.findViewById<LinearLayout>(R.id.reaction_container)
        val btnCopy = popupView.findViewById<TextView>(R.id.btn_copy) // 복사 버튼
        val btnForward = popupView.findViewById<TextView>(R.id.btn_forward) // 전달 버튼
        val btnCancel = popupView.findViewById<TextView>(R.id.btn_cancel) // 취소 버튼

        // AlertDialog로 팝업 표시
        val dialog = AlertDialog.Builder(this)
            .setView(popupView)
            .create()
        dialog.show()

        // 리액션 이모티콘 동적 추가
        for (reaction in reactions) {
            val textView = TextView(this).apply {
                text = reaction
                textSize = 24f
                gravity = Gravity.CENTER
                setPadding(22, 8, 22, 8)
                setOnClickListener {
                    updateReactions(senderMessagesRef, message, userId, reaction)
                    updateReactions(receiverMessagesRef, message, userId, reaction)
                    Toast.makeText(this@ChatActivity, "$reaction 리액션이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
            reactionContainer.addView(textView)
        }

        // 복사 버튼 클릭 이벤트
        btnCopy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Message", message.message)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "메시지가 복사되었습니다.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        // 전달 버튼 클릭 이벤트
        btnForward.setOnClickListener{
            shareMessage(message)
            dialog.dismiss()
        }

        // 취소 버튼 클릭 이벤트
        btnCancel.setOnClickListener {
            dialog.dismiss() // 팝업 닫기
        }

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

    fun showOptionsPopup(message: Message) {
        // 다이얼로그 레이아웃 초기화
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
            Toast.makeText(this, "메시지가 복사되었습니다.", Toast.LENGTH_SHORT).show()
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

    private fun showDeletePopup(message: Message) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("메시지 삭제")
        builder.setMessage("이 메시지를 삭제하시겠습니까?")
        builder.setPositiveButton("삭제") { dialog, _ ->

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

    fun blockUser(blockedUserId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val blockTime = System.currentTimeMillis()

        if (currentUserId != null) {
            val userRef = FirebaseDatabase.getInstance().reference
                .child("user").child(currentUserId).child("blockedUsers").child(blockedUserId)

            val blockData = mapOf("timestamp" to blockTime)

            userRef.setValue(blockData).addOnSuccessListener {
                Toast.makeText(this, "사용자를 차단했습니다.", Toast.LENGTH_SHORT).show()

                // 차단 성공 시 결과 전달
                val intent = Intent()
                intent.putExtra("refreshRequired", true) // 새로고침 필요 플래그
                setResult(Activity.RESULT_OK, intent)

            }.addOnFailureListener {
                Toast.makeText(this, "차단에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "로그인 상태를 확인하세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkIfBlocked(blockedUserId: String, callback: (Boolean) -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val userRef = FirebaseDatabase.getInstance().reference
            .child("user").child(currentUserId).child("blockedUsers").child(blockedUserId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isBlocked = snapshot.exists()  // 데이터가 있으면 차단된 상태
                callback(isBlocked)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatActivity", "Failed to check block status: ${error.message}")
                callback(false)
            }
        })
    }

    // 차단 해제 기능
    private fun unblockUser(blockedUserId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance().reference
            .child("user").child(currentUserId).child("blockedUsers").child(blockedUserId)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "차단을 해제했습니다.", Toast.LENGTH_SHORT).show()
                invalidateOptionsMenu() // 메뉴 다시 로드해서 "차단"으로 변경
            }
            .addOnFailureListener {
                Toast.makeText(this, "차단 해제에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    // 차단된 사용자 목록 가져오기
   private fun fetchBlockedUsers() {
       val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

       if (currentUserId != null) {
           val userRef = FirebaseDatabase.getInstance().reference.child("user").child(currentUserId)
               .child("blockedUsers")

           userRef.addValueEventListener(object : ValueEventListener { //
               override fun onDataChange(snapshot: DataSnapshot) {
                   blockedUserIds.clear()
                   blockTimeStamp = Long.MAX_VALUE

                   for (child in snapshot.children) {
                       val blockedUserId = child.key
                       val blockTime = child.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()

                       blockedUserId?.let {
                           blockedUserIds.add(blockedUserId)
                           blockTimeStamp = minOf(blockTimeStamp, blockTime)
                       }
                   }
                   Log.d("ChatActivity", "Blocked Users: $blockedUserIds")
               }

               override fun onCancelled(error: DatabaseError) {
                   Log.e("DatabaseError", "Failed to fetch blocked users: $error")
               }
           })
       }
   }

    override fun onResume() {
        super.onResume()
        // 메시지 읽음 상태 업데이트
        markMessagesAsRead(senderRoom, receiverRoom)
    }
    
    // 메세지 검색 기능
    private fun showSearchDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("메시지 검색")

        val input = EditText(this)
        input.hint = "검색어 입력"
        builder.setView(input)

        builder.setPositiveButton("검색") { _, _ ->
            val query = input.text.toString()
            searchMessage(query) // 검색 기능 실행
        }
        builder.setNegativeButton("취소", null)

        builder.show()
    }

    private fun searchMessage(query: String) {
        if (originalList == null) {
            originalList = ArrayList(messageList) // 원본 대화 저장
        }

        // 검색어를 포함하는 메시지는 isHighlighted = true
        messageList.forEach { message ->
            message.isHighlighted = message.message?.contains(query, ignoreCase = true) == true
        }
        messageAdapter.notifyDataSetChanged() // RecyclerView 갱신

    }

    private fun restoreOriginalList() {
        originalList?.let {
            messageAdapter.updateList(it)
            originalList = null // 원본 초기화
        }
    }

    // 액션바 버튼 기능 구현
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)

        // 차단 버튼 관련 처리
        val blockMenuItem = menu?.findItem(R.id.menu_block_user)
        if (blockMenuItem != null) {
            checkIfBlocked(receiverUid) { isBlocked ->
                blockMenuItem.title = if (isBlocked) "차단 해제" else "차단"
            }
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> {
                val intent = Intent()
                intent.putExtra("chatUpdated", true) // 결과 값으로 '갱신 필요' 플래그 전달
                setResult(Activity.RESULT_OK, intent)
                Log.d("ChatActivity", "setResult 호출됨") // 로그 추가

                if (originalList != null) {
                    restoreOriginalList() // 검색 전 리스트 복원
                }

                finish()
                true

            }

            R.id.menu_search -> {
                showSearchDialog()
                true
            }

            // 차단하기/해제 버튼
            R.id.menu_block_user -> {
                checkIfBlocked(receiverUid) { isBlocked ->
                    if (isBlocked) {
                        // ✅ 차단 해제 로직
                        AlertDialog.Builder(this)
                            .setTitle("차단 해제")
                            .setMessage("대화 상대의 차단을 해제하시겠습니까?")
                            .setPositiveButton("해제") { dialog, _ ->
                                unblockUser(receiverUid) // 차단 해제 실행
                                dialog.dismiss()
                            }
                            .setNegativeButton("취소") { dialog, _ -> dialog.dismiss() }
                            .show()
                    } else {
                        // ✅ 차단 실행
                        AlertDialog.Builder(this)
                            .setTitle("사용자 차단")
                            .setMessage("대화 상대를 차단하시겠습니까?")
                            .setPositiveButton("차단하기") { dialog, _ ->
                                blockUser(receiverUid) // 차단 실행
                                dialog.dismiss()
                            }
                            .setNegativeButton("취소") { dialog, _ -> dialog.dismiss() }
                            .show()
                    }
                }
                true
            }


            else -> super.onOptionsItemSelected(item)
        }

    }
}