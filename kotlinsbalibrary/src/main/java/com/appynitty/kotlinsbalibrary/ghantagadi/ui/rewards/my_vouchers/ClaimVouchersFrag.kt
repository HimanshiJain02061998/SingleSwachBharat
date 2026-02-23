package com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.my_vouchers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.databinding.FragmentClaimVouchersBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.Voucher
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClaimVouchersFrag : Fragment() {
    private var _binding: FragmentClaimVouchersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VouchersViewModel by activityViewModels()
    private lateinit var vouchersAdapter: VouchersAdapter
    private lateinit var walletId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClaimVouchersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentDate = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = formatter.format(currentDate)
        walletId = (activity as MyVouchersActivity).getWalletId()

        setupRecyclerView()
        subscribeActivityEvents()

        val cachedVouchers = viewModel.savedStateHandle.get<List<Voucher>>("claimedVouchers")

        if (!cachedVouchers.isNullOrEmpty()) {
            vouchersAdapter.updateVouchers(cachedVouchers)
        }

        if (!viewModel.isClaimedVouchersFetched) {
            viewModel.fetchClaimedVouchers(walletId, formattedDate)
        }
    }

    private fun subscribeActivityEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.claimedVouchersState.collect { state ->
                when (state) {
                    VouchersState.Empty -> {
                        Timber.d("onViewCreated: vouchers list is empty!")
                        binding.pbClaimedVoucher.visibility = View.GONE
                        vouchersAdapter.updateVouchers(emptyList())
                        binding.tvNoData.visibility = View.VISIBLE
                    }

                    is VouchersState.Error -> {
                        binding.pbClaimedVoucher.visibility = View.GONE
                        CustomToast.showErrorToast(requireActivity(), state.message)
                    }

                    is VouchersState.Loading -> {
                        binding.pbClaimedVoucher.visibility =
                            View.VISIBLE
                    }

                    is VouchersState.Success -> {
                        binding.pbClaimedVoucher.visibility = View.GONE
                        binding.tvNoData.visibility = View.GONE
                        vouchersAdapter.updateVouchers(state.vouchers)
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvClaimedVouchers.layoutManager = LinearLayoutManager(requireContext())
        vouchersAdapter = VouchersAdapter(emptyList(), 2) { couponCode ->
            val clipboardManager =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("txtCouponCode", couponCode)
            clipboardManager.setPrimaryClip(clipData)
            CustomToast.showInfoToast(requireContext(), "Text copied to clipboard")
        }

        binding.rvClaimedVouchers.adapter = vouchersAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateDate(formattedDate: String) {
        Timber.d( "updateDate called with: $formattedDate")
        viewModel.isClaimedVouchersFetched = false
        viewModel.fetchClaimedVouchers(walletId, formattedDate)
    }
}