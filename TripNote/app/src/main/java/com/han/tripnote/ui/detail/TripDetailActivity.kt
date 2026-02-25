package com.han.tripnote.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.han.tripnote.data.model.Trip
import com.han.tripnote.databinding.ActivityTripDetailBinding
import com.han.tripnote.ui.add.AddTripActivity
import com.han.tripnote.ui.viewmodel.TripViewModel
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import android.app.AlertDialog
import android.widget.EditText

class TripDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTripDetailBinding
    private lateinit var trip: Trip
    private val viewModel: TripViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTripDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        trip = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("trip", Trip::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("trip")!!
        }

        bindTripInfo()

        // 수정 버튼
        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, AddTripActivity::class.java)
            intent.putExtra("trip", trip) //수정 대상 전달
            startActivity(intent)
        }

        // 삭제 버튼
        binding.btnDelete.setOnClickListener {

            viewModel.removeTrip(trip)

            Snackbar.make(binding.root, "여행이 삭제되었습니다", Snackbar.LENGTH_LONG)
                .setAction("되돌리기") {

                    // 되돌리기 누르면 다시 추가
                    viewModel.upsertTrip(trip)
                }
                .addCallback(object : Snackbar.Callback() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                            finish()
                        }
                    }
                })
                .show()
        }

        binding.btnShare.setOnClickListener {

            val shareText = """
        ✈ TripNote 여행 정보
        장소: ${trip.title}
        여행 기간: ${trip.startDate} ~ ${trip.endDate}
        메모: ${trip.memo ?: "없음"}
         """.trimIndent()

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }

            startActivity(Intent.createChooser(intent, "여행 정보 공유"))
        }

        binding.ivDetailImage.setOnClickListener {
            val dialog = ImagePreviewDialog(trip.imageUri)
            dialog.show(supportFragmentManager, "ImagePreviewDialog")
        }

        binding.btnEditMemo.setOnClickListener {

            val editText = EditText(this)
            editText.setText(trip.memo ?: "")
            editText.setPadding(50, 40, 50, 40)

            AlertDialog.Builder(this)
                .setTitle("메모 수정")
                .setView(editText)
                .setPositiveButton("저장") { _, _ ->

                    val newMemo = editText.text.toString()
                    trip.memo = newMemo

                    // UI 즉시 반영
                    binding.tvMemo.text =
                        if (newMemo.isNotBlank()) newMemo
                        else "작성된 메모가 없습니다."

                    // 중요: 기존 ViewModel 업데이트 함수 사용
                    viewModel.upsertTrip(trip)

                }
                .setNegativeButton("취소", null)
                .show()
        }
    }

    private fun bindTripInfo() {
        binding.tvDetailTitle.text = trip.title
        binding.tvDetailLocation.text = trip.location
        binding.tvDetailDate.text = "${trip.startDate} ~ ${trip.endDate}"
        binding.tvMemo.text = trip.memo?.takeIf { it.isNotBlank() } ?: "작성된 메모가 없습니다."

        if (trip.imageUri != null) {
            Glide.with(this)
                .load(trip.imageUri)
                .into(binding.ivDetailImage)
        } else {
            binding.ivDetailImage.visibility = View.GONE
        }
    }

}