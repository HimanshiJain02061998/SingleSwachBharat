package com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.my_vouchers

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class VouchersPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ActiveVouchersFrag()
            1 -> ClaimVouchersFrag()
            2 -> ExpiredVouchersFrag()
            else -> ActiveVouchersFrag()
        }
    }
}