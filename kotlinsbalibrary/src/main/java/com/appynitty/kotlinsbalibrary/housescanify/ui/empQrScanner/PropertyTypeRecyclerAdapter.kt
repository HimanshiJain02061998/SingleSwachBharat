package com.appynitty.kotlinsbalibrary.housescanify.ui.empQrScanner

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.appynitty.kotlinsbalibrary.databinding.PropertyTypeEachItemBinding
import com.appynitty.kotlinsbalibrary.housescanify.model.response.PropertyType

class PropertyTypeRecyclerAdapter(private val propertyTypeList: List<PropertyType>) :
    RecyclerView.Adapter<PropertyTypeRecyclerAdapter.PropertyTypeViewHolder>() {

    private lateinit var listener: ItemSelectionListener
    private var languageId: String = "mr"
    fun setLanguageId(languageId: String) {
        this.languageId = languageId
        Log.d("TAG", "setLanguageId: $languageId")
    }

    fun setListener(listener: ItemSelectionListener) {
        this.listener = listener
    }

    inner class PropertyTypeViewHolder(private val binding: PropertyTypeEachItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(propertyType: PropertyType) {

            when (languageId) {
                "mr" -> {
                    binding.radioBtnItem.text = propertyType.Property_Type_Mar

                }
                "hi" -> {
                    binding.radioBtnItem.text = propertyType.Property_Type_Hin

                }
                else -> {
                    binding.radioBtnItem.text = propertyType.Property_Type

                }
            }

            // Checked selected radio button
            binding.radioBtnItem.isChecked = propertyTypeList[adapterPosition].isChecked

        }

        fun unCheckViews() {
            binding.radioBtnItem.isChecked = false
        }

        init {
            binding.radioBtnItem.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->

                if (b) {
                    isAnyItemChecked(adapterPosition)
                    propertyTypeList[adapterPosition].isChecked =
                        !propertyTypeList[adapterPosition].isChecked
                    listener.onItemSelected(propertyTypeList[adapterPosition])
                }
            }

        }
    }

    private fun isAnyItemChecked(position: Int) {

        val temp = propertyTypeList.indexOfFirst { it.isChecked }
        if (temp != position && temp >= 0) {
            propertyTypeList[temp].isChecked = false
            notifyItemChanged(temp, 2)
        }

    }

    override fun onBindViewHolder(
        holder: PropertyTypeViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty() && payloads[0] == 2) {
            holder.unCheckViews()
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyTypeViewHolder {
        val binding =
            PropertyTypeEachItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PropertyTypeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PropertyTypeViewHolder, position: Int) {
        val propertyType = propertyTypeList[position]
        holder.bind(propertyType)
    }

    override fun getItemCount(): Int {
        return propertyTypeList.size
    }

    interface ItemSelectionListener {
        fun onItemSelected(propertyType: PropertyType)
    }

}