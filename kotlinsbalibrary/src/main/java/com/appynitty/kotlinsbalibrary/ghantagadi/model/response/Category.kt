package com.appynitty.kotlinsbalibrary.ghantagadi.model.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    val catId: Int,
    val catLogoUrl: String,
    val catName: String,
    val smallLogoUrl: String
): Parcelable
