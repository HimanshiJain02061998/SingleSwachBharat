package com.appynitty.kotlinsbalibrary.common.utils

import android.app.Dialog
import android.os.Bundle
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.appynitty.kotlinsbalibrary.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MonthYearPickerDialogFragment: DialogFragment()  {
    private lateinit var monthPicker: NumberPicker
    private lateinit var yearPicker: NumberPicker
    private var listener: OnDateSetListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity?.layoutInflater?.inflate(R.layout.month_year_picker_dialog, null)

        monthPicker = view?.findViewById(R.id.monthPicker)!!
        yearPicker = view.findViewById(R.id.yearPicker)!!

        monthPicker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        yearPicker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        monthPicker.minValue = 0
        monthPicker.maxValue= 11
        monthPicker.value = currentMonth
        monthPicker.displayedValues = (0..11).map {
            SimpleDateFormat("MMM", Locale.getDefault()).format(
                Calendar.getInstance().apply { set(Calendar.MONTH, it) }.time
            )
        }.toTypedArray()

        yearPicker.minValue = currentYear - 10
        yearPicker.maxValue = currentYear + 10
        yearPicker.value = currentYear

        val builder = AlertDialog.Builder(requireActivity())
            .setView(view)
            .setTitle("Select Month and Year")
            .setPositiveButton("OK") { _, _ ->
                listener?.onDateSet(yearPicker.value, monthPicker.value)
            }
            .setNegativeButton("Cancel", null)

        return builder.create()
    }

    fun setListener(listener: OnDateSetListener) {
        this.listener = listener
    }

    interface OnDateSetListener {
        fun onDateSet(year: Int, month: Int)
    }
}