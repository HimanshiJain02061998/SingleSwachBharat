package com.appynitty.kotlinsbalibrary.ghantagadi.model.response

import com.google.gson.annotations.SerializedName

data class DealsCategoriesResponse(
    @SerializedName("catagoryLists")
    val categoryList: List<Category>,
    val code: Int,
    val message: String,
    val status: String
)
