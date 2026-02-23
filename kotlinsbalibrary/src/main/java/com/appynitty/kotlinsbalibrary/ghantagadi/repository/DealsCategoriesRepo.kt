package com.appynitty.kotlinsbalibrary.ghantagadi.repository

import com.appynitty.kotlinsbalibrary.ghantagadi.api.DealsCategoriesWebService
import javax.inject.Inject

class DealsCategoriesRepo@Inject constructor(private val dealsCategoriesWebService: DealsCategoriesWebService) {
    suspend fun getDealsCategories() = dealsCategoriesWebService.getDealsCategories()

    suspend fun getDealsCatWise(categoryId: Int, walletId: String) =
        dealsCategoriesWebService.getDealsCatWise(categoryId, walletId)
}