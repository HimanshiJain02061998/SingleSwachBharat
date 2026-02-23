package com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.my_vouchers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.databinding.ItemVoucherBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.Voucher
import com.bumptech.glide.Glide
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class VouchersAdapter(
    private var vouchers: List<Voucher>,
    private val voucherType: Int,
    private val onCopyCodeClick: (String) -> Unit
) : RecyclerView.Adapter<VouchersAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemVoucherBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(voucher: Voucher) {

            Glide.with(binding.root.context)
                .load(voucher.offerImgUrl)
                .placeholder(R.drawable.img_zomato)
                .into(binding.ivVoucher)

            binding.tvShopName.text = voucher.name
            binding.tvOfferTitle.text = voucher.offerTitle
            binding.tvCouponCode.text = voucher.couponCode

            val inputFormat = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss",
                Locale.getDefault()
            )
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

            try {
                val date = inputFormat.parse(voucher.validDatetime)
                if (date != null) {
                    binding.tvValidTill.text = outputFormat.format(date)
                } else {
                    binding.tvValidTill.text = voucher.validDatetime
                }
            } catch (e: ParseException) {
                binding.tvValidTill.text = voucher.validDatetime
                e.printStackTrace()
            }

            when (voucherType) {
                1 -> {
                    binding.tvVoucherStatus.visibility = ViewGroup.INVISIBLE
                }

                2 -> {
                    binding.tvVoucherStatus.visibility = ViewGroup.VISIBLE
                    binding.tvVoucherStatus.text =
                        ContextCompat.getString(binding.root.context, R.string.claimed_successfully)
                    binding.tvVoucherStatus.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.colorONDutyGreen
                        )
                    )
                }

                3 -> {
                    binding.tvVoucherStatus.visibility = ViewGroup.VISIBLE
                    binding.tvVoucherStatus.text =
                        ContextCompat.getString(binding.root.context, R.string.expired)
                    binding.tvVoucherStatus.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.colorOFFDutyRed
                        )
                    )
                }
            }
            binding.ibCopyCode.setOnClickListener {
                onCopyCodeClick(voucher.couponCode)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVoucherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return vouchers.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val voucher = vouchers[position]
        holder.bind(voucher)
    }

    fun updateVouchers(newVouchers: List<Voucher>) {
        val oldVouchers = this.vouchers

        val diffCallback = VouchersDiffCallback(oldVouchers, newVouchers)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.vouchers = newVouchers

        diffResult.dispatchUpdatesTo(this)
    }
}