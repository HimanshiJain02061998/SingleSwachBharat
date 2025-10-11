package com.appynitty.kotlinsbalibrary.common.ui.addUlb.selectCommon.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.ui.addUlb.selectCommon.model.CommonDistUlbModel

class DistUlbChildAdapter(private val mContext: Context, private var mList: List<CommonDistUlbModel?>?):
    RecyclerView.Adapter<DistUlbChildAdapter.ViewHolder>() {

    interface DistUlbChildAdapterListener {
        fun onItemClick(name: CommonDistUlbModel?)
    }

    companion object {

        lateinit var distUlbChildAdapterListener: DistUlbChildAdapterListener

        fun setAdapterAListener(distUlbChildAdapterListener: DistUlbChildAdapterListener) {
            this.distUlbChildAdapterListener = distUlbChildAdapterListener
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_spinner_city, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.tvCityName.text = mList?.get(position)?.name

        if (position == mList?.size?.minus(1)) holder.view1.visibility = View.INVISIBLE

        holder.itemView.setOnClickListener {
            distUlbChildAdapterListener.onItemClick(mList?.get(position))
        }


    }

    override fun getItemCount(): Int {
        return mList?.size ?: 0
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val tvCityName: AppCompatTextView = itemView.findViewById(R.id.tvCityName)
        val view1: View = itemView.findViewById(R.id.view1)
    }


}
