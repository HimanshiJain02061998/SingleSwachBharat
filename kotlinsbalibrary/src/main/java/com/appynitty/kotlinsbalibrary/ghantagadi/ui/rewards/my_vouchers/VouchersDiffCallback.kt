package com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.my_vouchers

import androidx.recyclerview.widget.DiffUtil
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.Voucher

class VouchersDiffCallback(
    private val oldList: List<Voucher>,
    private val newList: List<Voucher>
) : DiffUtil.Callback()  {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].couponCode == newList[newItemPosition].couponCode
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}