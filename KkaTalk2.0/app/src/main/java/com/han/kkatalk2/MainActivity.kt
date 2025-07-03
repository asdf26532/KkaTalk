package com.han.kkatalk2

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.han.kkatalk2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        // SharedPreferences 설정 불러와서 적용
        val appPrefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val isDarkMode = appPrefs.getBoolean("DARK_MODE", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

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
                R.id.setting -> replaceFragment(SettingFragment())

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

    // 커스텀 토스트
    fun Context.showCustomToast(message: String) {
        val toast = Toast(this)
        val inflater = LayoutInflater.from(this)
        val layout = inflater.inflate(R.layout.custom_toast_layout, null)

        val text = layout.findViewById<TextView>(R.id.toast_text)
        text.text = message

        toast.view = layout
        toast.duration = Toast.LENGTH_SHORT
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
        toast.show()
    }
