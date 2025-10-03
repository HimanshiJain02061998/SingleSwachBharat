package com.appynitty.kotlinsbalibrary.common.utils.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.appynitty.kotlinsbalibrary.databinding.FragmentAlertMessageDialogBinding


class AlertMessageDialogFrag : DialogFragment() {

    private lateinit var binding: FragmentAlertMessageDialogBinding
    private var listener: AlertMessageDialogCallBacks? = null
    fun setListener(listener: AlertMessageDialogCallBacks) {
        this.listener = listener
    }

    private var title: String = ""
    private var msg: String = ""
    private var type: String? = null

    fun setTitleAndMsg(title: String, msg: String, type: String) {
        this.title = title
        this.msg = msg
        this.type = type
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (savedInstanceState != null) {
            dismiss()
        }
        binding = FragmentAlertMessageDialogBinding.inflate(inflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDataToWidgets()
        registerClickEvents()
    }

    private fun registerClickEvents() {
        binding.yesBtn.setOnClickListener {
            type?.let { it1 -> listener?.onAlertDialogYesBtnClicked(it1) }
        }

        binding.noBtn.setOnClickListener {
            dismiss()
        }
    }

    private fun setDataToWidgets() {
        binding.title.text = title
        binding.message.text = msg
    }

    interface AlertMessageDialogCallBacks {
        fun onAlertDialogYesBtnClicked(type: String)
        fun onAlertDialogDismiss()
    }

    companion object {
        const val TAG = "AlertMessageDialogFrag"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)

        return dialog
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (listener != null)
            listener?.onAlertDialogDismiss()
    }


}