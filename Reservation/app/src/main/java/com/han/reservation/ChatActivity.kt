package com.han.reservation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.han.reservation.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = ArrayList<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSendButton()
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