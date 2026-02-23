package com.appynitty.kotlinsbalibrary.ghantagadi.model.response

data class VouchersResponse(
    val code: Int,
    val status: String,
    val message: String,
    val voucherDetails: List<Voucher>
)
