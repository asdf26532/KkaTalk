package com.han.kkaTalk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.han.kkaTalk.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        replaceFragment(HomeFragment())

        setContentView(binding.root)

        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.chatting -> replaceFragment(ChattingFragment())
                R.id.setting -> replaceFragment(SettingFragment())

                else -> {

                }
            }

            true
        }


    }

    private fun replaceFragment(fragment: Fragment) {

        val fragmentManager = supportFragmentManager
        val framentTransaction = fragmentManager.beginTransaction()
        framentTransaction.replace(R.id.frame_layout,fragment)
        framentTransaction.commit()


    }


}