package com.appynitty.kotlinsbalibrary.common.ui.archived

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.appynitty.kotlinsbalibrary.databinding.EachArchivedDataBinding


class ArchivedAdapter(val languageId: String?) :
    ListAdapter<ArchivedData, ArchivedAdapter.ArchivedViewHolder>(DiffCallback1()) {

    class ArchivedViewHolder(private val binding: EachArchivedDataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(archivedData: ArchivedData, languageId: String?) {

            binding.houseIdArchived.text = archivedData.referenceId
            if (languageId == "mr") {
                if (archivedData.errorMsgMr != null && archivedData.errorMsgMr != "")
                    binding.errorArchived.text = archivedData.errorMsgMr
                else
                    binding.errorArchived.text = archivedData.errorMsg

            } else {
                binding.errorArchived.text = archivedData.errorMsg
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchivedViewHolder {
        val binding =
            EachArchivedDataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArchivedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArchivedViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem, languageId)
    }

}

private class DiffCallback1 : DiffUtil.ItemCallback<ArchivedData>() {

    override fun areItemsTheSame(
        oldItem: ArchivedData,
        newItem: ArchivedData
    ) = oldItem.id == newItem.id


    override fun areContentsTheSame(
        oldItem: ArchivedData,
        newItem: ArchivedData
    ) = oldItem == newItem
}