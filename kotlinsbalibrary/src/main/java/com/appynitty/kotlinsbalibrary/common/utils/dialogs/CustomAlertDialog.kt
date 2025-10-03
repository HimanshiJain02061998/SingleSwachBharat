package com.appynitty.kotlinsbalibrary.common.utils.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.appynitty.kotlinsbalibrary.R

class CustomAlertDialog {

    companion object {
        private var dialog: AlertDialog? = null
        fun showSimpleDialog(
            context: Context
        ): View {
            val builder = AlertDialog.Builder(context)
            builder.setCancelable(false)
            val view = View.inflate(context, R.layout.layout_qr_result, null)
            builder.setView(view)
            dialog = builder.create()

            if (!dialog!!.isShowing) {
                dialog?.show()
            }
            return view
        }

        fun hideSimpleDialog() {
            if (dialog != null) {
                dialog?.dismiss()
            }
        }

        fun showUploadingDialog(context: Context): View? {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            return inflater.inflate(R.layout.layout_progress_bar, null)
        }

    }
}