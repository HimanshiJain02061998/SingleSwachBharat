package com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.about_coins

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.appynitty.kotlinsbalibrary.databinding.ItemAboutCoinsBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.AboutCoinDetail

class AboutCoinsAdapter(private var aboutCoinsList: List<AboutCoinDetail>) :
    RecyclerView.Adapter<AboutCoinsAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemAboutCoinsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(aboutCoinDetail: AboutCoinDetail) {
            binding.tvTitle.text = aboutCoinDetail.title
            binding.tvDescription.text = aboutCoinDetail.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemAboutCoinsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return aboutCoinsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val aboutCoinDetail = aboutCoinsList[position]
        holder.bind(aboutCoinDetail)
    }

    fun updateData(newAboutCoinsList: List<AboutCoinDetail>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = aboutCoinsList.size
            override fun getNewListSize() = newAboutCoinsList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return aboutCoinsList[oldItemPosition].id == newAboutCoinsList[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return aboutCoinsList[oldItemPosition] == newAboutCoinsList[newItemPosition]
            }
        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)
        aboutCoinsList = newAboutCoinsList
        diffResult.dispatchUpdatesTo(this)
    }
}