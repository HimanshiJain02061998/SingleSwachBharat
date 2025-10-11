package com.appynitty.kotlinsbalibrary.common.ui.addUlb.selectCommon.model

data class CommonDistUlbModel(val name: String,val id: Int)


data class CommonDistUlbParentModel(val parentName: String,val list : List<CommonDistUlbModel>)