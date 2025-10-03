package com.appynitty.kotlinsbalibrary.common.utils.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.databinding.FragmentGarbageTypeDialogBinding


class GarbageTypeDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentGarbageTypeDialogBinding
    private var listener: GarbageTypeDialogCallbacks? = null

    fun setListener(listener: GarbageTypeDialogCallbacks) {
        this.listener = listener
    }

    companion object {
        const val TAG = "GarbageTypeDialogFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentGarbageTypeDialogBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var garbageType: String? = null
        //submit btn insert or post to api
        binding.btnGarbageSubmit.setOnClickListener {

            when (binding.radioGroup.checkedRadioButtonId) {

                R.id.rb_bifurcate_garbage -> {
                    garbageType = "1"
                }
                R.id.rb_mixed_garbage -> {
                    garbageType = "0"
                }
                R.id.rb_no_garbage -> {
                    garbageType = "2"
                }
            }
            var note = binding.txtGarbageComments.text.toString()
            if (note.isBlank()) {
                note = ""
            }
            if (garbageType != null) {
                dismiss()
            }
            listener?.onSubmitGarbageTypeDialog(garbageType, note)
        }
    }


    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        listener?.onDialogDismiss()
    }
    interface GarbageTypeDialogCallbacks {
        fun onSubmitGarbageTypeDialog(garbageType: String?, note: String?)
        fun onDialogDismiss()
    }
}