package com.appynitty.kotlinsbalibrary.common.ui.workHistoryDetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.databinding.EachHistoryDetailItemBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.WorkHistoryDetailsResponse


class WorkHistoryDetailAdapter :
    ListAdapter<WorkHistoryDetailsResponse, WorkHistoryDetailAdapter.WorkHistoryDetailViewHolder>(
        DiffCallback()
    ) {

    private lateinit var listener: WorkHistoryDetailsClickListener
    fun setListener(listener: WorkHistoryDetailsClickListener) {
        this.listener = listener
    }

    class WorkHistoryDetailViewHolder(val binding: EachHistoryDetailItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(workHistoryDetailsResponse: WorkHistoryDetailsResponse) {
            with(workHistoryDetailsResponse) {
                with(binding) {

                    if (!vehicleNumber.isNullOrEmpty()) {

                        historyDetailsVehicle.visibility = android.view.View.VISIBLE
                        historyDetailsVehicle.text = buildString {
                            append(itemView.context.resources.getString(R.string.vehicle_number_txt))
                            append("  ")
                            append(vehicleNumber)
                        }
                    }else{
                        historyDetailsVehicle.visibility = android.view.View.GONE

                    }


                    when (type) {
                        "1" -> {
                            historyDetailsTime.setBackgroundResource(R.drawable.rounded_blue_button)

                            historyDetailsId.text = buildString {
                                append(itemView.context.resources.getString(R.string.house_id_txt))
                                append("  ")
                                append(Refid)
                            }

                        }
                        "3" -> {
                            historyDetailsTime.setBackgroundResource(R.drawable.rounded_orange_button)

                            historyDetailsId.text = buildString {
                                append(itemView.context.resources.getString(R.string.dump_yard_id_txt))
                                append("  ")
                                append(Refid)
                            }
                        }
                        "4" -> {
                            historyDetailsTime.setBackgroundResource(R.drawable.rounded_cyan_button)

                            historyDetailsId.text = buildString {
                                append(itemView.context.resources.getString(R.string.liquid_waste_id_txt))
                                append("  ")
                                append(Refid)
                            }
                        }

                        "5" -> {
                            historyDetailsTime.setBackgroundResource(R.drawable.rounded_pink_button)

                            historyDetailsId.text = buildString {
                                append(itemView.context.resources.getString(R.string.street_sweep_id_txt))
                                append("  ")
                                append(Refid)
                            }
                        }

                        "6" -> {
                            historyDetailsTime.setBackgroundResource(R.drawable.rounded_cyan_button)

                            historyDetailsId.text = buildString {
                                append(itemView.context.resources.getString(R.string.dump_yard_vehicle_id_txt))
                                append("  ")
                                append(Refid)
                            }
                        }

                        "12" -> {
                            historyDetailsTime.setBackgroundResource(R.drawable.rounded_violet_button)

                            historyDetailsId.text = buildString {
                                append(itemView.context.resources.getString(R.string.master_id))
                                append("  ")
                                append(Refid)
                            }
                        }
                    }
                    historyDetailsTime.setPadding(0, 0, 0, 0)
                    historyDetailsTime.text = time
                    historyDetailsName.text = name

                    historyDetailsArea.text = areaName
                    if (name.isNullOrEmpty())
                        historyDetailsName.visibility = android.view.View.GONE
                    else
                        historyDetailsName.visibility = android.view.View.VISIBLE


                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WorkHistoryDetailViewHolder {
        val binding =
            EachHistoryDetailItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WorkHistoryDetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkHistoryDetailViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
        holder.binding.historyDetailsTime.setOnClickListener {
            if (currentItem.time != null) {
                listener.onTimeBtnClicked(currentItem.time)
            }
        }
    }
}

private class DiffCallback : DiffUtil.ItemCallback<WorkHistoryDetailsResponse>() {

    override fun areItemsTheSame(
        oldItem: WorkHistoryDetailsResponse,
        newItem: WorkHistoryDetailsResponse
    ) = oldItem.Refid == newItem.Refid

    override fun areContentsTheSame(
        oldItem: WorkHistoryDetailsResponse,
        newItem: WorkHistoryDetailsResponse
    ) = oldItem == newItem

}

interface WorkHistoryDetailsClickListener {
    fun onTimeBtnClicked(time: String)
}
