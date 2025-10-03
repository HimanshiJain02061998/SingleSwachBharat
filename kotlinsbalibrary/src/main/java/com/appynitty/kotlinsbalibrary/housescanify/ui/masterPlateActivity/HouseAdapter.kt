package com.appynitty.kotlinsbalibrary.housescanify.ui.masterPlateActivity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.appynitty.kotlinsbalibrary.databinding.ItemHouseIdBinding

class HouseAdapter(
    private val houseList: MutableList<String>,
    deleteClickListener: DeleteClickListener
) :
    RecyclerView.Adapter<HouseAdapter.MyViewHolder>() {
    private val listener = deleteClickListener

    inner class MyViewHolder(private val binding: ItemHouseIdBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(houseId: String, position: Int) {
            binding.tvHouseId.text = houseId

            binding.ibDelete.setOnClickListener {
                listener.onClickDelete(houseId, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemHouseIdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val houseId = houseList[position]
        holder.bind(houseId, position)
    }

    override fun getItemCount(): Int {
        return houseList.size
    }

    fun removeItem(position: Int, houseId: String) {
        houseList.remove(houseId)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, houseList.size)
        listener.onItemDeleted(houseList)
    }

    interface DeleteClickListener {
        fun onClickDelete(houseId: String, position: Int)
        fun onItemDeleted(updatedList: MutableList<String>)
    }
}