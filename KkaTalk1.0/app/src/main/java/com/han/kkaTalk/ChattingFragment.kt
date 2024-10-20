package com.han.kkaTalk

import android.app.Activity
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
            intent.putExtra("nick", chatPreview.userNick)
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

        // Firebase에서 데이터 불러오기
        mDbRef.child("chats").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempChatList = ArrayList<ChatPreview>()

                for (chatSnapshot in snapshot.children) {
                    val lastMessageSnapshot = chatSnapshot.child("message").children.lastOrNull()

                    if (lastMessageSnapshot != null) {
                        val lastMessage = lastMessageSnapshot.getValue(Message::class.java)
                        val receiverUid = chatSnapshot.key?.replace(currentUserId, "")

                        if (receiverUid != null && lastMessage?.message != null) {
                            // 중복 확인 및 방지 (userName으로 중복된 채팅방 확인)
                            if (tempChatList.none { it.userUid == receiverUid }) {
                                mDbRef.child("user").child(receiverUid).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(userSnapshot: DataSnapshot) {
                                        val userName = userSnapshot.child("name").getValue(String::class.java) ?: "Unknown"
                                        val userNick = userSnapshot.child("nick").getValue(String::class.java) ?: "Unknown"

                                        // 중복 확인: 이미 해당 유저와의 채팅이 존재하는지 검사
                                        if (chatList.none { it.userName == userName }) {
                                            tempChatList.add(ChatPreview(userName, userNick, receiverUid, lastMessage.message ?: ""))

                                            // UI 업데이트
                                            chatList.clear()  // 전체 업데이트 전에 리스트 초기화
                                            chatList.addAll(tempChatList)
                                        }
                                        chatListAdapter.notifyDataSetChanged()
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e("ChattingFragment", "User data load cancelled: $error")
                                    }
                                })
                            }
                        } else {
                            Log.d("ChattingFragment", "Receiver UID or last message is null")
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChattingFragment", "Chat data load cancelled: $error")
            }
        })
    }
}
