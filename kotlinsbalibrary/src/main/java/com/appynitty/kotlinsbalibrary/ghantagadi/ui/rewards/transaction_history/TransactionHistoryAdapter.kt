package com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.transaction_history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.databinding.ItemTransactionHistoryBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.Transaction
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionHistoryAdapter(private var transactionList: List<Transaction>) :
    RecyclerView.Adapter<TransactionHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemTransactionHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {
            val dateStr = transaction.dateTime
            binding.tvShopName.text = transaction.msg
            binding.tvRewardPoints.text = transaction.points

            val points = transaction.points.toIntOrNull()

            binding.cvTransaction.setCardBackgroundColor(
                ContextCompat.getColor(binding.root.context, R.color.colorONDutyGreen)
            )
            binding.ivTransaction.setImageResource(R.drawable.icn_garbage)

            when (transaction.msg) {
                "Mixed Garbage" -> {
                    binding.cvTransaction.setCardBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.colorOFFDutyRed
                        )
                    )
                }
                "Segregated Garbage" -> {
                    binding.cvTransaction.setCardBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.colorONDutyGreen
                        )
                    )
                }
                else -> {
                    binding.cvTransaction.setCardBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.colorYellow
                        )
                    )
                    binding.ivTransaction.setImageResource(R.drawable.icn_reward)
                }
            }

            if (points != null && points < 0) {
                binding.tvRewardPoints.setTextColor(binding.root.context.getColor(R.color.colorOFFDutyRed))
            } else {
                binding.tvRewardPoints.setTextColor(binding.root.context.getColor(R.color.colorONDutyGreen))
            }

            try {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val formatter = SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm a", Locale.getDefault())
                val date = parser.parse(dateStr.split(".")[0])
                if (date != null) {
                    binding.tvTimeStamp.text = formatter.format(date)
                } else {
                    binding.tvTimeStamp.text = dateStr
                }
            } catch (e: ParseException) {
                e.printStackTrace()
                binding.tvTimeStamp.text = dateStr
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactionList[position]
        holder.bind(transaction)
    }

    fun updateData(newTransactionList: List<Transaction>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = transactionList.size
            override fun getNewListSize() = newTransactionList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return transactionList[oldItemPosition].dateTime == newTransactionList[newItemPosition].dateTime
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return transactionList[oldItemPosition] == newTransactionList[newItemPosition]
            }
        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)
        transactionList = newTransactionList
        diffResult.dispatchUpdatesTo(this)
    }
}