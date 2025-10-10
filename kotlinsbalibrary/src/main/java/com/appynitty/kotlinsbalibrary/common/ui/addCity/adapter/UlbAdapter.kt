package com.appynitty.kotlinsbalibrary.common.ui.addCity.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.model.response.ULBListItem


class UlbAdapter(private val mContext: Context, private var mList: List<ULBListItem?>?):
    RecyclerView.Adapter<UlbAdapter.ViewHolder>() {

    lateinit var ulbAdapterListener: UlbAdapterListener

    interface UlbAdapterListener {
        fun onItemClick(name: ULBListItem?)
    }

    fun setAdapterAListener(ulbAdapterListener: UlbAdapterListener) {
        this.ulbAdapterListener = ulbAdapterListener
    }


    fun filterList(filterlist: List<ULBListItem?>?) {
        mList = filterlist
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_spinner_city, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.tvCityName.text = mList?.get(position)?.uLBName

        holder.itemView.setOnClickListener {
            ulbAdapterListener.onItemClick(mList?.get(position))
        }


    }



    override fun getItemCount(): Int {
        return mList?.size ?: 0
    }



    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val tvCityName: AppCompatTextView = itemView.findViewById(R.id.tvCityName)
    }


}
