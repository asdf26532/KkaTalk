package com.han.kkatalk2

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.han.kkatalk2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }


        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.chatting -> replaceFragment(ChattingFragment())
                R.id.setting -> replaceFragment(TestFragment())

                else -> {
                }
            }
            true
        }
    }

    // 프래그먼트 설정
    private fun replaceFragment(fragment: Fragment) {

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.addToBackStack(null) // 백스택 추가
        fragmentTransaction.commit()
        Log.d("MainActivity", "Fragment replaced: ${fragment.javaClass.simpleName}") // 추가 로그
    }
}