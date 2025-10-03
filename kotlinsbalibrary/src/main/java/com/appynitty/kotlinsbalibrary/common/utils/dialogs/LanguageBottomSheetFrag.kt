package com.appynitty.kotlinsbalibrary.common.utils.dialogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.AppLanguage
import com.appynitty.kotlinsbalibrary.databinding.FragmentLanguageBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LanguageBottomSheetFrag : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentLanguageBottomSheetBinding
    private var languageId: String? = null
    private var preferredLang: String? = null
    private var listener: LanguageDialogCallbacks? = null

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

    fun setListener(listener: LanguageDialogCallbacks) {
        this.listener = listener
    }

    fun setPreferredLang(preferredLang: String) {
        this.preferredLang = preferredLang
    }

    companion object {

        const val TAG = "LanguageBottomSheetFrag"

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        if (savedInstanceState != null) {
            dismiss()
        }
        binding = FragmentLanguageBottomSheetBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (preferredLang) {

            binding.englishLang.text -> {
                binding.englishLang.isChecked = true
            }
            binding.marathiLang.text -> {
                binding.marathiLang.isChecked = true
            }

            binding.hindiLang.text -> {
                binding.hindiLang.isChecked = true

            }
        }
        clickEvents(view)
    }

    private fun clickEvents(view: View) {

        binding.submitBtn.setOnClickListener {
            when (binding.radioGroup.checkedRadioButtonId) {
                R.id.englishLang -> {
                    languageId = "en"
                }
                R.id.marathiLang -> {
                    languageId = "mr"
                }
                R.id.hindiLang -> {
                    languageId = "hi"
                }
            }
            Log.i(TAG, "clickEvents: $languageId")

            if (languageId != null) {
                val checkedBtn: RadioButton =
                    view.findViewById(binding.radioGroup.checkedRadioButtonId)

                val appLanguage = AppLanguage(languageId!!, checkedBtn.text.toString())
                listener?.onSubmitLanguageDialog(appLanguage)
            }
        }
    }

    interface LanguageDialogCallbacks {
        fun onSubmitLanguageDialog(appLanguage: AppLanguage)
    }
}