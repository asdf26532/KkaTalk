package com.han.reservation

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

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

