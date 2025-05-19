package com.han.kkatalk2

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class SettingFragment : Fragment() {


    // 전부 뜯어 고치기
    private lateinit var tvCurrentNick: TextView
    private lateinit var btnChangeNick: Button
    private lateinit var edtNewNick: EditText
    private lateinit var btnSaveNewNick: Button
    private lateinit var btnLogout: Button
    private lateinit var btnBlock: Button
    private lateinit var btnChangeProfile: Button
    private lateinit var btnDeleteProfile: Button
    private lateinit var btnDeleteAccount: Button
    private lateinit var ivProfile: ImageView

    private lateinit var tvCurrentStatus: TextView
    private lateinit var btnChangeStatus: Button
    private lateinit var edtNewStatus: EditText
    private lateinit var btnSaveNewStatus: Button

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    private lateinit var progressBar: ProgressBar

    private val TAG = "SettingFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference

        // UI 요소 초기화
        tvCurrentNick = view.findViewById(R.id.tv_current_nick)
        btnChangeNick = view.findViewById(R.id.btn_change_nick)
        edtNewNick = view.findViewById(R.id.edt_new_nick)
        btnSaveNewNick = view.findViewById(R.id.btn_save_new_nick)
        btnLogout = view.findViewById(R.id.btn_logout)
        btnChangeProfile = view.findViewById(R.id.btn_change_profile)
        ivProfile = view.findViewById(R.id.iv_profile)
        btnBlock = view.findViewById(R.id.btnManageBlockedUsers)
        btnDeleteProfile = view.findViewById(R.id.btn_delete_profile)
        btnDeleteAccount = view.findViewById(R.id.btn_delete_account)

        // 상태 메시지 관련 UI 요소 초기화
        tvCurrentStatus = view.findViewById(R.id.tv_current_status)
        btnChangeStatus = view.findViewById(R.id.btn_change_status)
        edtNewStatus = view.findViewById(R.id.edt_new_status)
        btnSaveNewStatus = view.findViewById(R.id.btn_save_new_status)

        // ProgressBar 초기화
        progressBar = view.findViewById(R.id.progress_bar)

        // 현재 프로필 사진 로드
        loadCurrentProfile()

        // 현재 닉네임 로드
        loadCurrentNick()

        // 현재 상태메세지 로드
        loadCurrentStatus()

        // 닉네임 변경 버튼 클릭 시
        btnChangeNick.setOnClickListener {
            edtNewNick.visibility = View.VISIBLE

            btnSaveNewNick.visibility = View.VISIBLE
        }

        // 저장 버튼 클릭 시
        btnSaveNewNick.setOnClickListener {
            val newNick = edtNewNick.text.toString().trim()
            if (newNick.isNotEmpty()) {
                updateNickname(newNick)
            }
        }

        // 상태 메시지 변경 버튼 클릭 시
        btnChangeStatus.setOnClickListener {
            edtNewStatus.visibility = View.VISIBLE
            btnSaveNewStatus.visibility = View.VISIBLE
        }

        // 상태 메시지 저장 버튼 클릭 시
        btnSaveNewStatus.setOnClickListener {
            val newStatus = edtNewStatus.text.toString().trim()
            if (newStatus.isNotEmpty()) {
                updateStatusMessage(newStatus)
            }
        }

        // 차단 관리 버튼 클릭 시
        btnBlock.setOnClickListener {
            val intent = Intent(requireContext(), BlockedUsersActivity::class.java)
            startActivity(intent)
        }

        // 로그아웃 버튼 클릭 시
        btnLogout.setOnClickListener {
            mAuth.signOut()  // Firebase에서 로그아웃
            val intent = Intent(activity, LoginActivity::class.java) // 로그인 화면으로 이동
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // 기존 액티비티 스택을 모두 제거
            startActivity(intent)
            activity?.finish()  // 현재 액티비티 종료
        }

        // 프로필 변경 버튼에 대한 클릭 리스너 설정
        btnChangeProfile.setOnClickListener {
            Log.d(TAG, "Profile change button clicked")
            selectProfileImage()
        }

        // 프로필 삭제 버튼
        btnDeleteProfile.setOnClickListener {
            deleteProfileImage()
        }

        // 회원 탈퇴 버튼
        btnDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }

        // 다크 모드 토글 추가
        val switchDarkMode: SwitchCompat = view.findViewById(R.id.switch_dark_mode)

        // SharedPreferences에서 다크 모드 상태 가져오기
        val sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val isDarkModeEnabled = sharedPreferences.getBoolean("DARK_MODE", false)

        // 토글 상태 적용
        switchDarkMode.isChecked = isDarkModeEnabled

        // 현재 다크 모드 적용
        applyDarkMode(isDarkModeEnabled)

        // 다크 모드 토글 변경 이벤트 처리
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            val editor = sharedPreferences.edit()
            editor.putBoolean("DARK_MODE", isChecked)
            editor.apply()

            applyDarkMode(isChecked)
        }


        return view
    }

    // 탈퇴 확인
    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("회원 탈퇴")
            .setMessage("정말 탈퇴하시겠습니까? 모든 정보가 삭제됩니다.")
            .setPositiveButton("탈퇴") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 계정 삭제
    private fun deleteAccount() {
        val user = mAuth.currentUser
        val uid = user?.uid

        Log.d("DeleteAccount", "user, uid: $user, $uid")

        if (user != null && uid != null) {
            // DB에서 데이터 삭제
            FirebaseDatabase.getInstance().reference.child("user").child(uid).removeValue()
                .addOnCompleteListener {
                    // Authentication에서 계정 삭제
                    user.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(), "회원 탈퇴 완료", Toast.LENGTH_SHORT).show()
                                mAuth.signOut()
                                startActivity(Intent(requireContext(), LoginActivity::class.java))
                                requireActivity().finish()
                            } else {
                                Toast.makeText(requireContext(), "회원 탈퇴 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
        } else {
            Toast.makeText( requireContext(), "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }



    // 다크 모드 토글
    private fun applyDarkMode(enabled: Boolean) {
        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    // 사용자 프로필 로드
    private fun loadCurrentProfile() {
        val userId = mAuth.currentUser?.uid.toString()

        if (userId.isNotEmpty()) {
            // 프로그레스바 보이기
            progressBar.visibility = View.VISIBLE

            mDbRef.child("user").child(userId).get().addOnSuccessListener { snapshot ->
                val currentUser = snapshot.getValue(User::class.java)
                val profileImageUrl = currentUser?.profileImageUrl

                if (!profileImageUrl.isNullOrEmpty()) {
                    // Firebase Storage 참조 생성
                    val storage = FirebaseStorage.getInstance(BuildConfig.STORAGE_BUCKET)
                    val storageRef = storage.getReferenceFromUrl(profileImageUrl)

                    // Firebase Storage에서 이미지 URL 다운로드
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        // Glide를 사용해 ImageView에 이미지 로드
                        Glide.with(this@SettingFragment)
                            .load(uri)
                            .placeholder(R.drawable.profile_default) // 기본 이미지 설정
                            .into(ivProfile)

                    }.addOnFailureListener {
                        // 실패 시 기본 이미지 로드
                        ivProfile.setImageResource(R.drawable.profile_default)
                    }.addOnCompleteListener {
                        // 다운로드 성공/실패 관계없이 프로그래스바 숨기기
                        progressBar.visibility = View.GONE
                    }
                } else {
                    // profileImageUrl이 null이거나 비어있는 경우 기본 이미지를 설정
                    ivProfile.setImageResource(R.drawable.profile_default)
                    progressBar.visibility = View.GONE
                }
            }.addOnFailureListener {
                // Firebase에서 데이터를 가져오는 데 실패한 경우 기본 이미지를 설정
                ivProfile.setImageResource(R.drawable.profile_default)
                progressBar.visibility = View.GONE
            }

        }
    }

    // 사진 선택 기능
    private fun selectProfileImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 이상: READ_MEDIA_IMAGES 권한 요청
            Log.d(TAG, "Requesting READ_MEDIA_IMAGES permission")
            requestPermissions(
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                REQUEST_CODE_SELECT_PHOTOS
            )
        } else {
            // Android 12 이하: READ_EXTERNAL_STORAGE 권한 요청
            Log.d(TAG, "Requesting READ_EXTERNAL_STORAGE permission")
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_SELECT_PHOTOS
            )
        }
    }

    // Firebase에 선택한 이미지를 업로드
    private fun uploadProfileImage(uri: Uri) {
        val userId = mAuth.currentUser?.uid.toString()
        val storageRef = Firebase.storage.reference.child("profileImages/$userId.jpg")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                // 업로드 성공 시 다운로드 URL 가져오기
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveProfileImageUrl(downloadUri.toString())
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to upload image to Firebase")
            }
    }

    // 다운로드 URL을 Firebase Realtime Database에 저장
    private fun saveProfileImageUrl(url: String) {
        val userId = mAuth.currentUser?.uid.toString()
        mDbRef.child("user").child(userId).child("profileImageUrl").setValue(url)
            .addOnSuccessListener {
                Log.d(TAG, "Profile image URL saved to database")
                // 이미지가 성공적으로 저장되었으면 로드
                loadCurrentProfile()
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to save image URL to database")
            }
    }

    // 선택된 사진 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult called with requestCode: $requestCode and resultCode: $resultCode")

        if (requestCode == REQUEST_CODE_SELECT_PHOTOS && resultCode == Activity.RESULT_OK) {
            data?.clipData?.let { clipData ->
                for (i in 0 until clipData.itemCount) {
                    val imageUri = clipData.getItemAt(i).uri
                    Log.d(TAG, "Selected image URI: $imageUri")
                    displaySelectedImage(imageUri)
                }
            } ?: data?.data?.let { imageUri ->
                Log.d(TAG, "Single image URI: $imageUri")
                displaySelectedImage(imageUri)
            }
        }
    }

    // 프로필 사진 선택
    private fun displaySelectedImage(uri: Uri) {
        Log.d(TAG, "Displaying selected image: $uri")
        Glide.with(this)
            .load(uri)
            .into(ivProfile)

        // Firebase에 이미지 업로드
        uploadProfileImage(uri)

    }

    companion object {
        private const val REQUEST_CODE_SELECT_PHOTOS = 2001
    }

    // 갤러리 권한
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult called with requestCode: $requestCode")

        if (requestCode == REQUEST_CODE_SELECT_PHOTOS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 승인되면 갤러리를 엽니다.
                Log.d(TAG, "Permission granted, opening gallery")
                openGallery()
            } else {
                // 권한이 거부된 경우, 사용자에게 알림을 표시하거나 대체 처리를 합니다.
                Log.d(TAG, "Permission denied, cannot open gallery")
            }
        }
    }

    private fun openGallery() {
        Log.d(TAG, "Opening gallery")
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_SELECT_PHOTOS)
    }

    // 프로필 사진 삭제
    private fun deleteProfileImage() {
        val userId = mAuth.currentUser?.uid.toString()
        if (userId.isNotEmpty()) {
            // Firebase Realtime Database에서 프로필 이미지 URL 삭제
            mDbRef.child("user").child(userId).child("profileImageUrl").removeValue()
                .addOnSuccessListener {
                    // ImageView를 기본 이미지로 변경
                    ivProfile.setImageResource(R.drawable.profile_default)

                    // 상태 알림
                    Toast.makeText(
                        requireContext(),
                        "기본 프로필 이미지가 적용되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    Log.e(TAG, "Failed to delete profile image URL")
                    Toast.makeText(
                        requireContext(),
                        "프로필 이미지를 삭제하는 중 오류가 발생했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            // 사용자 ID가 비어있을 경우 기본 이미지 설정 및 알림
            ivProfile.setImageResource(R.drawable.profile_default)
            Toast.makeText(
                requireContext(),
                "기본 프로필 이미지가 적용되었습니다.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // 사용자 닉네임
    private fun loadCurrentNick() {
        val userId = mAuth.currentUser?.uid.toString()
        if (userId != null) {
            mDbRef.child("user").child(userId).get().addOnSuccessListener { snapshot ->
                val currentUser = snapshot.getValue(User::class.java)
                tvCurrentNick.text = "현재 닉네임: ${currentUser?.nick}"
            }
        }
    }

    // 닉네임 변경
    private fun updateNickname(newNick: String) {
        val userId = mAuth.currentUser?.uid.toString()
        if (userId != null) {
            mDbRef.child("user").child(userId).child("nick").setValue(newNick).addOnSuccessListener {
                tvCurrentNick.text = "현재 닉네임: $newNick"
                edtNewNick.visibility = View.GONE
                btnSaveNewNick.visibility = View.GONE
            }
        }
    }

    // 현재 상태 메시지 로드
    private fun loadCurrentStatus() {
        val userId = mAuth.currentUser?.uid.toString()
        if (userId.isNotEmpty()) {
            mDbRef.child("user").child(userId).get().addOnSuccessListener { snapshot ->
                val currentUser = snapshot.getValue(User::class.java)
                tvCurrentStatus.text = "현재 상태 메시지: ${currentUser?.statusMessage ?: "없음"}"
            }.addOnFailureListener { exception ->
                tvCurrentStatus.text = "현재 상태 메시지: 없음"
                Log.e(TAG, "Failed to load status message", exception)
            }
        }
    }

    // 상태 메시지 업데이트
    private fun updateStatusMessage(newStatus: String) {
        val userId = mAuth.currentUser?.uid.toString()
        if (userId.isNotEmpty()) {
            mDbRef.child("user").child(userId).child("statusMessage").setValue(newStatus)
                .addOnSuccessListener {
                    tvCurrentStatus.text = "현재 상태 메시지: $newStatus"
                    edtNewStatus.visibility = View.GONE
                    btnSaveNewStatus.visibility = View.GONE
                }
        }
    }

}
