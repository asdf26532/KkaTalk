package com.han.kkaTalk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.han.kkaTalk.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        replaceFragment(Home())

        setContentView(binding.root)

        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.home -> replaceFragment(Home())
                R.id.chatting -> replaceFragment(Chatting())
                R.id.setting -> replaceFragment(Setting())

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