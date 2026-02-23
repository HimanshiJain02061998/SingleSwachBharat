package com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.category_wise_deals

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.databinding.ItemDealsBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.OfferDetailsCatWise
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class OfferAdapter(
    private var offerDetailsList: List<OfferDetailsCatWise>,
    private val onItemClicked: (OfferDetailsCatWise) -> Unit
) :
    RecyclerView.Adapter<OfferAdapter.OfferViewHolder>() {
    inner class OfferViewHolder(val binding: ItemDealsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(offerDetails: OfferDetailsCatWise) {
            Glide.with(binding.root.context)
                .load(offerDetails.offerImgUrl)
                .into(binding.ivDeal)

            binding.tvRewardPoints.text = offerDetails.reqPoints.toString()
            binding.tvDescDeal.text = offerDetails.offerTitle
            val validTill = offerDetails.offerEndTime

            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

            val date = inputFormat.parse(validTill)
            val formattedDate = outputFormat.format(date!!)
            binding.tvValidTill.text =
                binding.root.context.getString(R.string.validTill, formattedDate)
            if (offerDetails.offerRedeem) {
                binding.tvAlreadyRedeemed.visibility = View.VISIBLE
                binding.btnRedeem.visibility = View.INVISIBLE
            } else {
                binding.tvAlreadyRedeemed.visibility = View.INVISIBLE
                binding.btnRedeem.visibility = View.VISIBLE
            }
            binding.btnRedeem.setOnClickListener {
                onItemClicked(offerDetails)
            }
            binding.root.setOnClickListener {
                onItemClicked(offerDetails)
            }
            binding.tvDetails.setOnClickListener {
                onItemClicked(offerDetails)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val binding = ItemDealsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OfferViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return offerDetailsList.size
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        val offerDetails = offerDetailsList[position]
        holder.bind(offerDetails)
    }

    fun updateOffers(newOfferDetailsList: List<OfferDetailsCatWise>) {

        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = offerDetailsList.size
            override fun getNewListSize() = newOfferDetailsList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return offerDetailsList[oldItemPosition].offerId == newOfferDetailsList[newItemPosition].offerId
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return offerDetailsList[oldItemPosition] == newOfferDetailsList[newItemPosition]
            }
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        offerDetailsList = newOfferDetailsList
        diffResult.dispatchUpdatesTo(this)
    }

}