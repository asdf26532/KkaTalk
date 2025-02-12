    package com.han.kkaTalk

    import android.app.Activity
    import android.app.AlertDialog
    import android.content.Intent
    import android.os.Bundle
    import android.util.Log
    import androidx.fragment.app.Fragment
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import androidx.recyclerview.widget.LinearLayoutManager
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.database.ChildEventListener
    import com.google.firebase.database.DataSnapshot
    import com.google.firebase.database.DatabaseError
    import com.google.firebase.database.DatabaseReference
    import com.google.firebase.database.FirebaseDatabase
    import com.google.firebase.database.ValueEventListener
    import com.han.kkaTalk.databinding.FragmentChattingBinding

    class ChattingFragment : Fragment() {

        private lateinit var binding: FragmentChattingBinding
        private lateinit var chatListAdapter: ChatListAdapter
        private lateinit var chatList: ArrayList<ChatPreview> // 마지막 메시지를 포함한 채팅 목록
        private lateinit var mDbRef: DatabaseReference
        private lateinit var mAuth: FirebaseAuth

        private val blockedUserIds: ArrayList<String> = ArrayList()

        companion object {
            const val REQUEST_CHAT_UPDATE = 1001
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            binding = FragmentChattingBinding.inflate(inflater, container, false)

            // RecyclerView 세팅
            chatList = ArrayList()
            chatListAdapter = ChatListAdapter(chatList, { chatPreview ->
                // 채팅방으로 이동
                /*val intent = Intent(activity, ChatActivity::class.java)*/
                val intent = Intent(context, ChatActivity::class.java)

                intent.putExtra("nick", chatPreview.userNick)
                intent.putExtra("uId", chatPreview.userUid)
                intent.putExtra("profileImageUrl", chatPreview.profileImageUrl)
                startActivityForResult(intent, REQUEST_CHAT_UPDATE) // REQUEST_CHAT_UPDATE는 식별용 상수
            }, { chatPreview ->
                // 삭제 처리
                deleteChatRoom(chatPreview)
            })

            binding.rvChat.layoutManager = LinearLayoutManager(activity)
            binding.rvChat.adapter = chatListAdapter

            // 파이어베이스에서 데이터 가져오기
            mAuth = FirebaseAuth.getInstance()
            mDbRef = FirebaseDatabase.getInstance().reference

            listenForChatUpdates()
            loadChatPreviews()

            return binding.root
        }
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            Log.d("ChattingFragment", "onActivityResult 호출됨") // 로그 추가
            if (requestCode == REQUEST_CHAT_UPDATE && resultCode == Activity.RESULT_OK) {
                // 갱신 필요 플래그 확인
                val chatUpdated = data?.getBooleanExtra("chatUpdated", false) ?: false
                if (chatUpdated) {
                    loadChatPreviews() // 채팅 목록 갱신
                    Log.d("loadChatPreviews", "loadChatPreviews 호출") // 로그 추가
                }
            }
        }

        private fun listenForChatUpdates() {
            val currentUserId = mAuth.currentUser?.uid ?: return

            mDbRef.child("chats").addChildEventListener(object : ChildEventListener {
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    loadChatPreviews() // 읽음 상태 변경 시 채팅 목록 갱신
                }

                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    Log.d("ChattingFragment", "Chat removed: ${snapshot.key}")

                    // 삭제된 채팅방 목록에서 제거
                    val chatKey = snapshot.key ?: return
                    val receiverUid = chatKey.replace(currentUserId, "")

                    chatList.removeAll { it.userUid == receiverUid }
                    chatListAdapter.notifyDataSetChanged()
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChattingFragment", "Chat update listener cancelled: ${error.message}")
                }
            })
        }

        private fun deleteChatRoom(chatPreview: ChatPreview) {
            val currentUserId = mAuth.currentUser?.uid ?: return
            val chatKey = currentUserId + chatPreview.userUid


            Log.d("ChatKeys", "Deleting chatKey: $chatKey")

            // 확인 다이얼로그 표시
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("채팅방 삭제")
            builder.setMessage("채팅방을 정말 삭제하시겠습니까?")
            builder.setPositiveButton("삭제") { _, _ ->
                mDbRef.child("chats").child(chatKey)
                    .removeValue()
                    .addOnSuccessListener {
                    // UI 업데이트
                    chatList.remove(chatPreview)
                    chatListAdapter.notifyDataSetChanged()
                        binding.rvChat.postDelayed({
                            loadChatPreviews()
                        }, 300)  // 0.3초 후 실행

                }.addOnFailureListener { e ->
                    Log.e("ChattingFragment", "Failed to delete chat keys: ${e.message}")
                }
            }
            builder.setNegativeButton("취소", null)
            builder.show()
        }

        private fun fetchBlockedUsers(onComplete: () -> Unit) {
            val currentUserId = mAuth.currentUser?.uid ?: return

            val blockedUsersRef = mDbRef.child("user").child(currentUserId).child("blockedUsers")
            blockedUsersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    blockedUserIds.clear()
                    for (child in snapshot.children) {
                        val blockedUserId = child.key
                        if (blockedUserId != null) {
                            blockedUserIds.add(blockedUserId)
                        }
                    }
                    onComplete() // 차단 목록을 가져온 후 수행할 작업
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChattingFragment", "Failed to fetch blocked users: ${error.message}")
                }
            })
        }



        private fun loadChatPreviews() {
            val currentUserId = mAuth.currentUser?.uid ?: return

            fetchBlockedUsers {
                mDbRef.child("chats").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val tempChatList = ArrayList<ChatPreview>()

                        for (chatSnapshot in snapshot.children) {
                            val chatKey = chatSnapshot.key ?: continue

                            if (!chatKey.startsWith(currentUserId)) continue

                            mDbRef.child("chats").child(chatKey).get().addOnSuccessListener { chatData ->
                                if (!chatData.exists() || chatData.childrenCount == 0L) {
                                    Log.d("ChatDebug", "ChatKey $chatKey has no messages, skipping")
                                    return@addOnSuccessListener
                                }
                            }
                            Log.d("ChatDebug", "Checking if chatKey $chatKey is empty: ${chatSnapshot.childrenCount}")


                            val receiverUid = chatKey.replace(currentUserId, "")

                            val lastMessageSnapshot = chatSnapshot.child("message").children.lastOrNull()
                            if (lastMessageSnapshot != null) {
                                val lastMessage = lastMessageSnapshot.getValue(Message::class.java)

                                val lastMessageSender = lastMessageSnapshot.child("senderId").value.toString()
                                val isBlocked = blockedUserIds.contains(receiverUid)

                                if (blockedUserIds.contains(receiverUid) && lastMessageSender == receiverUid) {
                                    Log.d("ChattingFragment", "Blocked user sent last message, hiding chat: $receiverUid")
                                    continue
                                }

                                val messageContent: String = when {
                                    isBlocked -> "(차단된 사용자입니다)"
                                    lastMessage?.deleted == true -> "삭제된 메시지입니다"
                                    lastMessage?.fileUrl != null -> "(사진)" // 이미지 메시지일 경우
                                    else -> lastMessage?.message ?: ""
                                }


                                val unreadCount = if (isBlocked) {
                                    0 // 차단된 사용자 메시지는 항상 0으로 설정
                                } else {
                                    chatSnapshot.child("message").children
                                        .filter {
                                            it.child("mread")
                                                .getValue(Boolean::class.java) == false &&
                                                    it.child("receiverId").value == currentUserId &&
                                                    !blockedUserIds.contains(it.child("senderId").value)
                                        }
                                        .count()
                                }
                                mDbRef.child("user").child(receiverUid)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(userSnapshot: DataSnapshot) {
                                            val userName = userSnapshot.child("name").getValue(String::class.java) ?: "(알 수 없음)"
                                            val userNick = userSnapshot.child("nick").getValue(String::class.java) ?: "(알 수 없음)"
                                            val profileImageUrl = userSnapshot.child("profileImageUrl").getValue(String::class.java) ?: "default_url"
                                            val lastMessageTime = lastMessageSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L

                                            if (tempChatList.none { it.userUid == receiverUid }) {
                                                tempChatList.add(
                                                    ChatPreview(
                                                        userName, userNick, receiverUid,
                                                        messageContent, lastMessageTime,
                                                        profileImageUrl, unreadCount
                                                    )
                                                )
                                            }

                                            // UI 업데이트
                                            chatList.clear()
                                            chatList.addAll(tempChatList.sortedByDescending { it.lastMessageTime })
                                            chatListAdapter.notifyDataSetChanged()
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Log.e("ChattingFragment", "User data load cancelled: $error")
                                        }
                                    })
                            }
                        }
                    }


                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChattingFragment", "Chat data load cancelled: $error")
                    }
                })
            }
        }
    }