package com.appynitty.kotlinsbalibrary.common.ui.addCity.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.model.response.DistrictListItem


class DistrictAdapter(private val mContext: Context, private var mList: List<DistrictListItem?>?):
    RecyclerView.Adapter<DistrictAdapter.ViewHolder>() {

    lateinit var districtAdapterListener: DistrictAdapterListener

    interface DistrictAdapterListener {
        fun onItemClick(name: DistrictListItem?)
    }

    fun setAdapterAListener(districtAdapterListener: DistrictAdapterListener) {
        this.districtAdapterListener = districtAdapterListener
    }


    fun filterList(filterlist:List<DistrictListItem?>?) {
        mList = filterlist
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_spinner_city, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.tvCityName.text = mList?.get(position)?.districtName

        holder.itemView.setOnClickListener {
            districtAdapterListener.onItemClick(mList?.get(position))
        }


    }



    override fun getItemCount(): Int {
        return mList?.size ?: 0
    }



    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val tvCityName: AppCompatTextView = itemView.findViewById(R.id.tvCityName)
    }


}
