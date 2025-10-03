package com.appynitty.kotlinsbalibrary.common.utils.dialogs

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.appynitty.kotlinsbalibrary.R

class PopUpAlertDialog {

    companion object {
        fun showSimpleDialog(
            context: Context,
            title: String?,
            message: String?,
            positiveListener: DialogInterface.OnClickListener?
        ) {
            val positiveText = context.resources.getString(R.string.ok_txt)
            val builder = AlertDialog.Builder(context)
            if (!title.isNullOrEmpty()) {
                builder.setTitle(title)
            }
            if (!message.isNullOrEmpty()) {
                builder.setMessage(message)
            }
            builder.setCancelable(false).setPositiveButton(positiveText, positiveListener).create()
                .show()
        }
    }
}