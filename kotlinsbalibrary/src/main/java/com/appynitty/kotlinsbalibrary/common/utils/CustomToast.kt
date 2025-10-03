package com.appynitty.kotlinsbalibrary.common.utils

import android.content.Context
import android.view.Gravity
import com.shashank.sony.fancytoastlib.FancyToast


class CustomToast {

    companion object {

        fun showSuccessToast(context: Context, message: String) {

            val fancyToast = FancyToast.makeText(
                context,
                message,
                FancyToast.LENGTH_SHORT,
                FancyToast.SUCCESS,
                false
            )
            fancyToast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 50)
            fancyToast.show()
        }

        fun showErrorToast(context: Context, message: String) {

            val fancyToast = FancyToast.makeText(
                context,
                message,
                FancyToast.LENGTH_SHORT,
                FancyToast.ERROR,
                false
            )
            fancyToast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 50)
            fancyToast.show()
        }

        fun showWarningToast(context: Context, message: String) {

            val fancyToast = FancyToast.makeText(
                context,
                message,
                FancyToast.LENGTH_SHORT,
                FancyToast.WARNING,
                false
            )
            fancyToast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 50)
            fancyToast.show()
        }
    }
}