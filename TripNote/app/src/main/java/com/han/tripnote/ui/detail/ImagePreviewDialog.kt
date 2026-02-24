package com.han.tripnote.ui.detail

import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.han.tripnote.databinding.DialogImagePreviewBinding

class ImagePreviewDialog(
    private val imageUri: String?
) : DialogFragment() {

    private var _binding: DialogImagePreviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.window?.attributes?.windowAnimations = android.R.style.Animation_Dialog
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogImagePreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Glide.with(this)
            .load(imageUri)
            .into(binding.ivPreview)

        binding.ivPreview.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}