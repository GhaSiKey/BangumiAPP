package com.gaoshiqi.image.viewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gaoshiqi.image.databinding.BottomSheetImageMenuBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * 图片操作底部弹窗
 */
class ImageMenuBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetImageMenuBinding? = null
    private val binding get() = _binding!!

    private var onSaveClickListener: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetImageMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()

        (dialog as? BottomSheetDialog)?.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        )?.apply {
            setBackgroundResource(android.R.color.transparent)
        }
    }

    private fun setupClickListeners() {
        binding.optionSaveImage.setOnClickListener {
            dismiss()
            onSaveClickListener?.invoke()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * 设置保存图片点击回调
     */
    fun setOnSaveClickListener(listener: () -> Unit): ImageMenuBottomSheet {
        onSaveClickListener = listener
        return this
    }

    companion object {
        const val TAG = "ImageMenuBottomSheet"

        fun newInstance(): ImageMenuBottomSheet {
            return ImageMenuBottomSheet()
        }
    }
}
