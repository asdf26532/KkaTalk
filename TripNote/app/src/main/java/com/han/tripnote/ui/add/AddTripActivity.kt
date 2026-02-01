package com.han.tripnote.ui.add

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.han.tripnote.databinding.ActivityAddTripBinding
import java.util.UUID

class AddTripActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTripBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddTripBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 저장 버튼
        binding.btnSave.setOnClickListener {

            val intent = Intent().apply {
                putExtra("id", UUID.randomUUID().toString())
                putExtra("title", binding.etTitle.text.toString())
                putExtra("location", binding.etLocation.text.toString())
                putExtra("startDate", binding.etStartDate.text.toString())
                putExtra("endDate", binding.etEndDate.text.toString())
            }

            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}