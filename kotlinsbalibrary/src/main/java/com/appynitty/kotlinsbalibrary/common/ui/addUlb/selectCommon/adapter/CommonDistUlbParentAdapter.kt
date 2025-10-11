package com.appynitty.kotlinsbalibrary.common.ui.addUlb.selectCommon.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.ui.addUlb.selectCommon.model.CommonDistUlbParentModel

class CommonDistUlbParentAdapter(private val mContext: Context, private var mList: List<CommonDistUlbParentModel?>?):
    RecyclerView.Adapter<CommonDistUlbParentAdapter.ViewHolder>() {

    fun updateList(newList: List<CommonDistUlbParentModel>) {
        mList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.grouping_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvTittle.text = mList?.get(position)?.parentName
       val parentAdapter = DistUlbChildAdapter(mContext, mList?.get(position)?.list)
        holder.rv.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        holder.rv.adapter = parentAdapter

    }

    override fun getItemCount(): Int {
        Log.d("adapsize","adapter size ${ mList?.size}")
        return mList?.size ?: 0
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val tvTittle: AppCompatTextView = itemView.findViewById(R.id.tvTittle)
        val rv: RecyclerView = itemView.findViewById(R.id.rvchild)
    }


}
