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
import com.appynitty.kotlinsbalibrary.databinding.FragmentExpiredVouchersBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.Voucher
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpiredVouchersFrag : Fragment() {
    private var _binding: FragmentExpiredVouchersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VouchersViewModel by activityViewModels()
    private lateinit var vouchersAdapter: VouchersAdapter
    private lateinit var walletId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpiredVouchersBinding.inflate(inflater, container, false)
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

        val cachedVouchers = viewModel.savedStateHandle.get<List<Voucher>>("expiredVouchers")

        if (!cachedVouchers.isNullOrEmpty()) {
            vouchersAdapter.updateVouchers(cachedVouchers)
        }

        if (!viewModel.isExpiredVouchersFetched) {
            viewModel.fetchExpiredVouchers(walletId, formattedDate)
        }
    }

    private fun subscribeActivityEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.expiredVouchersState.collect { state ->
                when (state) {
                    VouchersState.Empty -> {
                        Timber.d("onViewCreated: vouchers list is empty!")
                        binding.pbExpiredVouchers.visibility = View.GONE
                        vouchersAdapter.updateVouchers(emptyList())
                        binding.tvNoData.visibility = View.VISIBLE
                    }

                    is VouchersState.Error -> {
                        binding.pbExpiredVouchers.visibility = View.GONE
                        CustomToast.showErrorToast(requireActivity(), state.message)
                    }

                    is VouchersState.Loading -> {
                        binding.pbExpiredVouchers.visibility =View.VISIBLE
                    }

                    is VouchersState.Success -> {
                        binding.pbExpiredVouchers.visibility = View.GONE
                        binding.tvNoData.visibility = View.GONE
                        vouchersAdapter.updateVouchers(state.vouchers)
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvExpiredVouchers.layoutManager = LinearLayoutManager(requireContext())
        vouchersAdapter = VouchersAdapter(emptyList(), 3) { couponCode ->
            val clipboardManager =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("txtCouponCode", couponCode)
            clipboardManager.setPrimaryClip(clipData)
            CustomToast.showInfoToast(requireContext(), "Text copied to clipboard")
        }

        binding.rvExpiredVouchers.adapter = vouchersAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateDate(formattedDate: String) {
        viewModel.isExpiredVouchersFetched = false
        viewModel.fetchExpiredVouchers(walletId, formattedDate)
    }
}