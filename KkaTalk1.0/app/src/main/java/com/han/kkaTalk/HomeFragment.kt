package com.han.kkaTalk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var userList: ArrayList<User>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // RecyclerView 초기화
        userRecyclerView = view.findViewById(R.id.rv_user)
        userRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 사용자 목록 초기화 (여기에 실제 데이터 로드 로직 추가 필요)
        userList = arrayListOf(
            User("홍길동", "hong@kakao.com", "1", "길동이"),
            User("이순신", "lee@kakao.com", "2", "이순신")
        )

        // UserAdapter 초기화 및 설정
        userAdapter = UserAdapter(requireContext(), userList) { user ->
            // 사용자 클릭 시 ChattingFragment로 이동
            val chatFragment = ChattingFragment()
            val args = Bundle().apply {
                putString("name", user.name)
                putString("uId", user.uId)
                putString("nick", user.nick)
            }
            chatFragment.arguments = args

            // 현재 프래그먼트를 교체하여 채팅 프래그먼트로 이동
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, chatFragment)
                .addToBackStack(null) // 뒤로가기 기능 추가
                .commit()
        }

        userRecyclerView.adapter = userAdapter

        return view
    }
}
