package com.appynitty.kotlinsbalibrary.ghantagadi.repository

import com.appynitty.kotlinsbalibrary.ghantagadi.api.VouchersWebService
import javax.inject.Inject

class VouchersRepo @Inject constructor(private val vouchersWebService: VouchersWebService) {
    suspend fun getActiveVouchers(walletId: String, date: String) =
        vouchersWebService.getActiveVouchers(walletId, date)

    suspend fun getClaimedVouchers(walletId: String, date: String) =
        vouchersWebService.getClaimedVouchers(walletId, date)

    suspend fun getExpiredVouchers(walletId: String, date: String) =
        vouchersWebService.getExpiredVouchers(walletId, date)
}