package com.appynitty.kotlinsbalibrary.common.model.response

import com.google.gson.annotations.SerializedName

data class GetUlbListResponse(

	@field:SerializedName("Status")
	val status: String? = null,

	@field:SerializedName("MessageHindi")
	val messageHindi: String? = null,

	@field:SerializedName("ULBList")
	val uLBList: List<ULBListItem?>? = null,

	@field:SerializedName("Message")
	val message: String? = null,

	@field:SerializedName("MessageMar")
	val messageMar: String? = null,

	@field:SerializedName("Code")
	val code: Int? = null
)

data class ULBListItem(

	@field:SerializedName("Appid")
	val appid: Int? = null,

	@field:SerializedName("ULBName")
	val uLBName: String? = null
)
