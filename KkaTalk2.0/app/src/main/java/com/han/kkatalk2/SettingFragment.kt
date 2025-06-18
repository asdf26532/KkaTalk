package com.han.kkatalk2

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.han.kkatalk2.databinding.FragmentTestBinding
import com.kakao.sdk.user.UserApiClient

class SettingFragment : Fragment() {
    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userRef: DatabaseReference
    private lateinit var prefs: SharedPreferences
    private lateinit var storage: FirebaseStorage
    private var userId: String = ""
    private lateinit var progressBar: ProgressBar

    private val TAG = "SettingFragment"
    private val defaultProfileImageUrl = "${BuildConfig.STORAGE_BUCKET}/profile_default.png"

    private lateinit var switchDarkMode: SwitchCompat

    private val appPrefs by lazy {
        requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        storage = FirebaseStorage.getInstance(BuildConfig.STORAGE_BUCKET)
        progressBar = binding.progressBar

        // 유저 정보 불러오기
        val currentUser = auth.currentUser
        if (currentUser != null) {
            userId = currentUser.uid
            Log.d(TAG,"userid: $userId")
        } else {
            // SharedPreferences에서 userid 가져오기
            userId = prefs.getString("userId", null) ?: ""
            Log.d(TAG,"userid: $userId")
        }

        userRef = database.getReference("user").child(userId)


        // 유저 정보 불러오기
        if (userId.isNotEmpty()) {
            loadUserData()
        } else {
            Toast.makeText(requireContext(), "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_LONG).show()
            // 로그인 화면으로 이동
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        // 닉네임 변경 버튼 클릭 시
        binding.btnChangeNick.setOnClickListener {
            binding.edtNewNick.visibility = View.VISIBLE
            binding.btnSaveNewNick.visibility = View.VISIBLE
        }

        // 닉네임 저장 버튼 클릭 시
        binding.btnSaveNewNick.setOnClickListener {
            val newNick = binding.edtNewNick.text.toString().trim()
            if (newNick.isNotEmpty()) {
                updateNickname(newNick)
            }
        }

        // 상태 메시지 변경
        binding.btnChangeStatus.setOnClickListener {
            binding.edtNewStatus.visibility = View.VISIBLE
            binding.btnSaveNewStatus.visibility = View.VISIBLE
        }

        // 상태 메시지 저장
        binding.btnSaveNewStatus.setOnClickListener {
            val newStatus = binding.edtNewStatus.text.toString().trim()
            if (newStatus.isNotEmpty()) {
                updateStatusMessage(newStatus)
            }
        }

        // 차단 관리
        binding.btnManageBlockedUsers.setOnClickListener {
            val intent = Intent(requireContext(), BlockedUsersActivity::class.java)
            startActivity(intent)
        }

        // 프로필 변경
        binding.btnChangeProfile.setOnClickListener {
            chageProfileImage()
        }

        // 프로필 삭제
        binding.btnDeleteProfile.setOnClickListener {
            deleteProfileImage()
        }

        // 로그아웃
        binding.btnLogout.setOnClickListener {
            handleLogout()
        }

        // 회원 탈퇴
        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }

        // 다크모드 스위치 연결
        switchDarkMode = view.findViewById(R.id.switch_dark_mode)

        // 초기 상태 반영
        val isDarkMode = appPrefs.getBoolean("DARK_MODE", false)
        switchDarkMode.isChecked = isDarkMode
        applyDarkMode(isDarkMode)

        // 리스너 연결
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            appPrefs.edit().putBoolean("DARK_MODE", isChecked).apply()
            applyDarkMode(isChecked)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 현재 사용자 정보 불러오기
    private fun loadUserData() {
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Fragment가 아직 화면에 붙어있고, binding이 null이 아닐 때만 처리
                if (!isAdded || _binding == null) {
                    Log.e(TAG, "loadUserData() 호출 시 fragment가 detach 상태이거나 binding이 null입니다.")
                    return
                }

                val nick = snapshot.child("nick").getValue(String::class.java) ?: "닉네임 없음"
                val status = snapshot.child("statusMessage").getValue(String::class.java) ?: "상태메시지 없음"
                val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                binding.tvCurrentNick.text = "현재 닉네임: $nick"
                binding.tvCurrentStatus.text = "현재 상태 메시지: $status"
                binding.progressBar.visibility = View.VISIBLE

                if (!profileImageUrl.isNullOrEmpty()) {
                    val storageRef = storage.getReferenceFromUrl(profileImageUrl)

                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        if (!isAdded || _binding == null) return@addOnSuccessListener

                        Glide.with(this@SettingFragment)
                            .load(uri)
                            .placeholder(R.drawable.profile_default)
                            .into(binding.ivProfile)

                    }.addOnFailureListener {
                        if (!isAdded || _binding == null) return@addOnFailureListener

                        binding.ivProfile.setImageResource(R.drawable.profile_default)
                    }.addOnCompleteListener {
                        if (!isAdded || _binding == null) return@addOnCompleteListener

                        binding.progressBar.visibility = View.GONE
                    }
                } else {
                    binding.ivProfile.setImageResource(R.drawable.profile_default)
                    binding.progressBar.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded || _binding == null) return
                Toast.makeText(requireContext(), "사용자를 찾을 수 없습니다", Toast.LENGTH_LONG).show()
            }
        })
    }

    // 현재 사용자 정보 불러오기
   /* private fun loadUserData() {
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nick = snapshot.child("nick").getValue(String::class.java) ?: "닉네임 없음"
                val status = snapshot.child("statusMessage").getValue(String::class.java) ?: "상태메시지 없음"
                val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                binding.tvCurrentNick.text = "현재 닉네임: $nick"
                binding.tvCurrentStatus.text = "현재 상태 메시지: $status"
                binding.progressBar.visibility = View.VISIBLE

                if (!profileImageUrl.isNullOrEmpty()) {
                    // Firebase Storage 참조 생성
                    val storageRef = storage.getReferenceFromUrl(profileImageUrl)

                    // Firebase Storage에서 이미지 URL 다운로드
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        // Glide를 사용해 ImageView에 이미지 로드
                        Glide.with(this@SettingFragment)
                            .load(uri)
                            .placeholder(R.drawable.profile_default) // 기본 이미지 설정
                            .into(binding.ivProfile)

                    }.addOnFailureListener {
                        // 실패 시 기본 이미지 로드
                        binding.ivProfile.setImageResource(R.drawable.profile_default)
                    }.addOnCompleteListener {
                        // 프로그래스바 숨기기
                        progressBar.visibility = View.GONE
                    }
                } else {
                    // profileImageUrl이 null이거나 비어있는 경우 기본 이미지를 설정
                    binding.ivProfile.setImageResource(R.drawable.profile_default)
                    progressBar.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // 실패 처리
                Toast.makeText(requireContext(), "사용자를 찾을 수 없습니다", Toast.LENGTH_LONG).show()
            }
        })
    }*/

    // 닉네임 변경
    private fun updateNickname(newNick: String) {
        if (userId.isNotEmpty()) {
            userRef.child("nick").setValue(newNick)
                .addOnSuccessListener {
                    binding.tvCurrentNick.text = "현재 닉네임: $newNick"
                    binding.edtNewNick.text.clear()
                    binding.edtNewNick.visibility = View.GONE
                    binding.btnSaveNewNick.visibility = View.GONE
                    Toast.makeText(requireContext(), "닉네임이 변경되었습니다.", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "닉네임 변경에 실패했습니다.", Toast.LENGTH_LONG).show()
                }
        }
    }

    // 상태 메시지 업데이트
    private fun updateStatusMessage(newStatus: String) {
        if (userId.isNotEmpty()) {
            userRef.child("statusMessage").setValue(newStatus)
                .addOnSuccessListener {
                    binding.tvCurrentStatus.text = "현재 상태 메시지: $newStatus"
                    binding.edtNewStatus.visibility = View.GONE
                    binding.btnSaveNewStatus.visibility = View.GONE
                }
        }
        Toast.makeText(requireContext(), "상태 메시지가 변경되었습니다.", Toast.LENGTH_LONG).show()
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
                // 권한이 승인되면 갤러리 열기
                Log.d(TAG, "Permission granted, opening gallery")
                openGallery()
            } else {
                // 권한이 거부된 경우
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

    // 사진 선택 기능
    private fun chageProfileImage() {
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
            .into(binding.ivProfile)

        // Firebase에 이미지 업로드
        uploadProfileImage(uri)

    }

    // Firebase에 선택한 이미지를 업로드
    private fun uploadProfileImage(uri: Uri) {
        val storageRef = storage.reference.child("profileImages/$userId.jpg")

        // Storage에서 기존 이미지 삭제
        storageRef.delete()
            .addOnSuccessListener {
                Log.d(TAG, "Firebase Storage 이미지 삭제 성공")
            }
            .addOnFailureListener {
                Log.e(TAG, "Firebase Storage 이미지 삭제 실패 또는 존재하지 않음: ${it.message}")
            }

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

    // 이미지 주소 DB에 저장
    private fun saveProfileImageUrl(url: String) {
        userRef.child("profileImageUrl").setValue(url)
            .addOnSuccessListener {
                Log.d(TAG, "Profile image URL saved to database")
                // 이미지가 성공적으로 저장되었으면 로드
                loadUserData()
                Toast.makeText(requireContext(),"프로필 사진이 변경되었습니다",Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to save image URL to database")
                Toast.makeText(requireContext(),"프로필 사진 변경 실패",Toast.LENGTH_LONG).show()
            }
    }

    // 프로필 사진 삭제
    private fun deleteProfileImage() {
        val storageRef = storage.reference.child("profileImages/$userId.jpg")

        // Storage에서 이미지 삭제
        storageRef.delete()
            .addOnSuccessListener {
                Log.d(TAG, "Firebase Storage 이미지 삭제 성공")
            }
            .addOnFailureListener {
                Log.e(TAG, "Firebase Storage 이미지 삭제 실패 또는 존재하지 않음: ${it.message}")
            }

        // DB에서 프로필 이미지 URL 삭제
        if (userId.isNotEmpty()) {
            userRef.child("profileImageUrl").setValue(defaultProfileImageUrl)
                .addOnSuccessListener {
                    loadUserData()
                    // 상태 알림
                    Toast.makeText(requireContext(), "기본 프로필 이미지가 적용되었습니다.", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Log.e(TAG, "Failed to delete profile image URL")
                    Toast.makeText(requireContext(), "프로필 이미지를 삭제하는 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show()
                }
        } else {
            // 사용자 ID가 비어있을 경우 기본 이미지 설정 및 알림
            binding.ivProfile.setImageResource(R.drawable.profile_default)
            Toast.makeText(requireContext(), "기본 프로필 이미지가 적용되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }


    // 통합 로그아웃 처리
    private fun handleLogout() {
        // FirebaseAuth 로그아웃
        auth.signOut()

        // 카카오 로그아웃 시도 (카카오 로그아웃 실패해도 무시)
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Log.w("TestFragment", "카카오 로그아웃 실패: ${error.message}")
            }
            // SharedPreferences 초기화 및 로그인 화면 이동
            prefs.edit().clear().apply()
            redirectToLogin()
        }
    }

    // 로그아웃 후처리
    private fun redirectToLogin() {
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
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
        if (userId.isNotEmpty()) {

            // 1. DB에서 유저 데이터 삭제
            userRef.removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // 2. auth계정 삭제 (자체 회원가입 유저인 경우만)
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        if (currentUser != null) {
                            currentUser.delete()
                                .addOnCompleteListener { authTask ->
                                    if (authTask.isSuccessful) {
                                        Toast.makeText(requireContext(), "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                                        FirebaseAuth.getInstance().signOut()
                                        redirectToLogin()
                                    } else {
                                        Toast.makeText(requireContext(), "계정 삭제 실패: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                        } else {
                            // SNS 로그인 사용자: DB만 삭제됨
                            Toast.makeText(requireContext(), "회원 데이터 삭제 완료되었습니다. (SNS 로그인 계정)", Toast.LENGTH_SHORT).show()
                            redirectToLogin()
                        }
                    } else {
                        Toast.makeText(requireContext(), "데이터베이스 삭제 실패", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            Toast.makeText(requireContext(), "로그인된 사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 다크모드
    private fun applyDarkMode(enabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

}