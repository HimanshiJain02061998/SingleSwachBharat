package com.appynitty.kotlinsbalibrary.common.model.response

import com.google.gson.annotations.SerializedName

data class GetDistrictListResponse(

	@field:SerializedName("Status")
	val status: String? = null,

	@field:SerializedName("MessageHindi")
	val messageHindi: String? = null,

	@field:SerializedName("Message")
	val message: String? = null,

	@field:SerializedName("DistrictList")
	val districtList: List<DistrictListItem?>? = null,

	@field:SerializedName("MessageMar")
	val messageMar: String? = null,

	@field:SerializedName("Code")
	val code: Int? = null
)

data class DistrictListItem(

	@field:SerializedName("DistrictName")
	val districtName: String? = null,

	@field:SerializedName("Disid")
	val disid: Int? = null
)
