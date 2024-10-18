package com.han.kkaTalk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChattingBinding.inflate(inflater, container, false)

        // RecyclerView 세팅
        chatList = ArrayList()
        chatListAdapter = ChatListAdapter(chatList) { chatPreview ->
            // 채팅방으로 이동
            val intent = Intent(activity, ChatActivity::class.java)
            intent.putExtra("name", chatPreview.userName)
            intent.putExtra("uId", chatPreview.userUid)
            startActivity(intent)
        }

        binding.rvChat.layoutManager = LinearLayoutManager(activity)
        binding.rvChat.adapter = chatListAdapter

        // 파이어베이스에서 데이터 가져오기
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference

        loadChatPreviews()

        return binding.root
    }
    private fun loadChatPreviews() {
        val currentUserId = mAuth.currentUser?.uid ?: return

        mDbRef.child("chats").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempChatList = mutableListOf<ChatPreview>()
                Log.d("ChattingFragment", "Chat snapshot children count: ${snapshot.childrenCount}")

                // 모든 채팅을 순회
                for (chatSnapshot in snapshot.children) {
                    Log.d("ChattingFragment", "Chat snapshot key: ${chatSnapshot.key}")

                    // 마지막 메시지 확인
                    val lastMessageSnapshot = chatSnapshot.child("message").children.lastOrNull()
                    if (lastMessageSnapshot != null) {
                        val lastMessage = lastMessageSnapshot.getValue(Message::class.java)
                        val receiverId = chatSnapshot.child("receiverId").getValue(String::class.java)

                        // receiverId가 현재 유저 ID가 아닌 경우에만 진행
                        if (receiverId != null && receiverId != currentUserId) {
                            // 상대방의 유저 이름을 가져오는 부분
                            mDbRef.child("users").child(receiverId).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                    val userName = userSnapshot.child("name").getValue(String::class.java) ?: "Unknown"

                                    Log.d("ChattingFragment", "User Name: $userName")

                                    // 채팅 프리뷰 추가
                                    if (lastMessage != null) {
                                        // 중복 체크를 위한 유니크한 키 사용
                                        if (tempChatList.none { it.userUid == receiverId }) {
                                            tempChatList.add(ChatPreview(userName, receiverId, lastMessage.message))
                                        }
                                    }

                                    // 모든 사용자 이름이 로드되었는지 확인
                                    if (tempChatList.size == snapshot.childrenCount.toInt()) {
                                        chatList.clear()
                                        chatList.addAll(tempChatList)
                                        chatListAdapter.notifyDataSetChanged()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("ChattingFragment", "Error fetching user: ${error.message}")
                                }
                            })
                        }
                    } else {
                        Log.d("ChattingFragment", "No last message found for chat: ${chatSnapshot.key}")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChattingFragment", "Error loading chats: ${error.message}")
            }
        })
    }
}
