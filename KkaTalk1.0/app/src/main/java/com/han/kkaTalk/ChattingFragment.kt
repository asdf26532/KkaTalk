package com.han.kkaTalk

import android.content.Intent
import android.os.Bundle
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
    private lateinit var chatList: ArrayList<ChatPreview>  // 마지막 메시지를 포함한 채팅 목록
    private lateinit var mDbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        // 마지막 메시지를 가져오는 로직 (senderRoom과 receiverRoom 사용)
        mDbRef.child("chats").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()

                for (chatSnapshot in snapshot.children) {
                    val lastMessageSnapshot = chatSnapshot.child("message").children.lastOrNull()
                    if (lastMessageSnapshot != null) {
                        val lastMessage = lastMessageSnapshot.getValue(Message::class.java)
                        val receiverUid = chatSnapshot.key?.replace(currentUserId, "")

                        if (receiverUid != null) {
                            // 상대방의 유저 이름을 가져오는 부분
                            mDbRef.child("users").child(receiverUid)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(userSnapshot: DataSnapshot) {
                                        chatList.clear()
                                        val userName = userSnapshot.child("name").getValue(String::class.java) ?: "Unknown"
                                        if (lastMessage != null) {
                                            // 채팅 리스트에 추가
                                            chatList.add(ChatPreview(userName, receiverUid, lastMessage.message))
                                        }
                                        chatListAdapter.notifyDataSetChanged()
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        // 에러 처리
                                    }
                                })
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // 에러 처리
            }
        })
    }
}
