package com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.redeem

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.databinding.LayoutRedeemSuccessBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.RedeemOfferData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Locale

class RedeemSuccessBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: LayoutRedeemSuccessBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutRedeemSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle: RedeemOfferData? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("redeemResponse", RedeemOfferData::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("redeemResponse")
        }

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

        val date = bundle?.validDatetime?.let { inputFormat.parse(it) }
        val formattedDateString = date?.let { outputFormat.format(it) }

        binding.tvCouponCode.text = bundle?.couponCode
        binding.tvOfferDiscount.text = bundle?.offerTitle
        binding.tvOfferValidity.text = getString(R.string.validTill, formattedDateString)

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.btnCopyCode.setOnClickListener {
            val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("voucher_code", binding.tvCouponCode.text)
            clipboard.setPrimaryClip(clip)
            CustomToast.showInfoToast(requireActivity(), "Code copied!")
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        (activity as? RedeemActivity)?.onBottomSheetDismissed()
    }

    companion object {
        const val TAG = "RedeemSuccessBottomSheet"
    }
}