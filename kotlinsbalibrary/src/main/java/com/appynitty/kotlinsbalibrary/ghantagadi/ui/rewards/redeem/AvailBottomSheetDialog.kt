package com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.redeem

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.databinding.DialogCitySpinnerBinding
import com.appynitty.kotlinsbalibrary.databinding.DialogComfirmBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AvailBottomSheetDialog : BottomSheetDialogFragment() {
    private var _binding: DialogComfirmBinding? = null
    private val binding get() = _binding!!
    private lateinit var listener: ConfirmButtonListener
    private var reqPoints: Int = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ConfirmButtonListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ConfirmButtonListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogComfirmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        return dialog
    }
    override fun onStart() {
        super.onStart()

        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                sheet.setBackgroundColor(Color.TRANSPARENT)
                val layoutParams = sheet.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.setMargins(40, 0, 40, 40)
                sheet.layoutParams = layoutParams

                val behavior = BottomSheetBehavior.from(sheet)
                behavior.peekHeight = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._300sdp)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reqPoints = arguments?.getInt(ARG_REQ_POINTS) ?: 0

        binding.tvDialogTitle.text = getString(R.string.avail_for, reqPoints)
        binding.tvDialogMessage.text = getString(R.string.txt_confirm_coupon_purchase)

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnConfirm.setOnClickListener {
            dismiss()
            listener.onConfirmButtonClicked(reqPoints)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_REQ_POINTS = "reqPoints"

        fun newInstance(amount: Int): AvailBottomSheetDialog {
            val fragment = AvailBottomSheetDialog()
            val args = Bundle()
            args.putInt(ARG_REQ_POINTS, amount)
            fragment.arguments = args
            return fragment
        }
    }
}

interface ConfirmButtonListener {
    fun onConfirmButtonClicked(reqPoints: Int)
}