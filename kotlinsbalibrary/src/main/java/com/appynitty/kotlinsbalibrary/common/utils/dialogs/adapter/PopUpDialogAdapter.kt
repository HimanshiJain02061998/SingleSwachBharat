package com.appynitty.kotlinsbalibrary.common.utils.dialogs.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.VehicleTypeResponse


private const val TAG = "PopUpDialogAdapter"

class PopUpDialogAdapter(private val mList: List<VehicleTypeResponse>) :
    RecyclerView.Adapter<PopUpDialogAdapter.PopUpDialogViewHolder>() {

    private var popUpDialogItemClickListener: PopUpDialogItemClickListener? = null

    fun setListener(popUpDialogItemClickListener: PopUpDialogItemClickListener) {
        this.popUpDialogItemClickListener = popUpDialogItemClickListener
    }

    inner class PopUpDialogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textViewVehicleType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopUpDialogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.custom_textview, parent, false)
        return PopUpDialogViewHolder(view)
    }

    override fun onBindViewHolder(holder: PopUpDialogViewHolder, position: Int) {
        with(holder) {

            with(mList[position]) {
                textView.text = this.description
                itemView.setOnClickListener {

                    this.vtId?.let { vtId ->
                        this.description?.let { it1 ->
                            popUpDialogItemClickListener?.onDialogItemClicked(
                                vtId,
                                it1
                            )
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount: " + mList.size)
        return mList.size
    }


    interface PopUpDialogItemClickListener {
        fun onDialogItemClicked(vehicleId: String, vehicleTypeName: String)
    }
}