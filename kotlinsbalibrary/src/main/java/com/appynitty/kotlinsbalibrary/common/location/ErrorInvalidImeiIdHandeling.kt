package com.appynitty.kotlinsbalibrary.common.location

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


private val _imeiChanged = MutableStateFlow(false)
val imeiChanged: StateFlow<Boolean> = _imeiChanged

fun updateImeiChange(value: Boolean) {
    _imeiChanged.value = value
}