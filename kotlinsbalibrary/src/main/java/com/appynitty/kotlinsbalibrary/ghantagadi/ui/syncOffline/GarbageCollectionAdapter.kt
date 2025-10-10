package com.appynitty.kotlinsbalibrary.ghantagadi.ui.syncOffline

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.appynitty.kotlinsbalibrary.common.MyApplication.Companion.APP_ID
import com.appynitty.kotlinsbalibrary.databinding.EachGcLayoutBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class GarbageCollectionAdapter :
    ListAdapter<SyncOfflineData, GarbageCollectionAdapter.GCViewHolder>(DiffCallback()) {

    var empType: String? = null
    private var historyClickListener: HistoryClickListener? = null
    fun setHistoryCardClickListener(historyClickListener: HistoryClickListener) {
        this.historyClickListener = historyClickListener
    }
//
//    private var syncOfflineClickListener   : SyncOfflineCardClickedListener? = null
//    fun setSyncOfflineClickListener(syncOfflineCardClickedListener: SyncOfflineCardClickedListener){
//        this.syncOfflineClickListener = syncOfflineClickListener
//    }

    class GCViewHolder(private val binding: EachGcLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(syncOfflineData: SyncOfflineData, empType: String?) {

            binding.apply {
                historyDateTxt.text = syncOfflineData.date.subSequence(0, 2)
                historyMonthTxt.text = syncOfflineData.date.subSequence(3, 6)

                if (syncOfflineData.userType == 0) {

                    if (empType != null) {
                        when (empType) {
                            "N" -> {
                                //ghanta gadi
                                if (APP_ID == "3201") {
                                    dyCollection.text = syncOfflineData.offlineDumpCount.toString()
                                    houseCollection.visibility = View.VISIBLE
                                    houseCollectionLbl.visibility = View.VISIBLE

//                                val countText = syncOfflineData.offlineGarbageCollectionCount.toString() + " *"
//                                val spannable = SpannableString(countText)
//                                spannable.setSpan(
//                                    ForegroundColorSpan(Color.RED),
//                                    countText.length - 1, // start index of '*'
//                                    countText.length,     // end index (exclusive)
//                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                                )
//
//                                houseCollection.text = spannable

                                    val calendar = Calendar.getInstance()
                                    val hour = calendar.get(Calendar.HOUR_OF_DAY)

                                    val todayDate = Date() // current date object
                                    val todayFormatted =
                                        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(
                                            todayDate
                                        )

                                    // handle cases like "08-Oct-2025" → convert to "dd-MM-yyyy"
                                    val inputFormat =
                                        SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
                                    val outputFormat =
                                        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

                                    var itemDateFormatted = ""
                                    try {
                                        val parsedDate = inputFormat.parse(syncOfflineData.date)
                                        itemDateFormatted = outputFormat.format(parsedDate!!)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    // ✅ Check: today's date and between 4 AM – 8 PM
                                    if (itemDateFormatted == todayFormatted && hour in 4..20) {
                                        val countText =
                                            syncOfflineData.offlineGarbageCollectionCount.toString() + " *"

                                        val spannable = SpannableString(countText)
                                        spannable.setSpan(
                                            ForegroundColorSpan(Color.RED),
                                            countText.length - 1,
                                            countText.length,
                                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                        )

                                        houseCollection.text = spannable
                                    } else {
                                        // show normal count without red asterisk
                                        houseCollection.text =
                                            syncOfflineData.offlineGarbageCollectionCount.toString()
                                    }
                                } else {
                                    dyCollection.text = syncOfflineData.offlineDumpCount.toString()
                                    houseCollection.visibility = View.VISIBLE
                                    houseCollectionLbl.visibility = View.VISIBLE
                                    houseCollection.text =
                                        syncOfflineData.offlineGarbageCollectionCount.toString()
                                }
                            }

                            "S" -> {
                                //street
                                dyCollection.text = syncOfflineData.offlineDumpCount.toString()
                                ssCollectionLbl.visibility = View.VISIBLE
                                ssCollection.visibility = View.VISIBLE
                                ssCollection.text =
                                    syncOfflineData.offlineGarbageCollectionCount.toString()
                            }

                            "L" -> {
                                //liquid
                                dyCollection.text = syncOfflineData.offlineDumpCount.toString()
                                lwcCollection.visibility = View.VISIBLE
                                lwcCollectionLbl.visibility = View.VISIBLE
                                lwcCollection.text =
                                    syncOfflineData.offlineGarbageCollectionCount.toString()
                            }

                            "D" -> {
                                //liquid
                                dyCollection.visibility = View.GONE
                                dyCollectionLbl.visibility = View.GONE
                                dsCollection.visibility = View.VISIBLE
                                dsCollectionLbl.visibility = View.VISIBLE
                                dsCollection.text =
                                    syncOfflineData.offlineGarbageCollectionCount.toString()
                            }
                        }
                    }
                } else if (syncOfflineData.userType == 1) {

                    //house scanify

                    Log.d("TAG", "bind: $syncOfflineData")
                    dyCollection.text = syncOfflineData.offlineDumpCount.toString()
                    houseCollection.visibility = View.VISIBLE
                    houseCollectionLbl.visibility = View.VISIBLE
                    houseCollection.text =
                        syncOfflineData.offlineGarbageCollectionCount.toString()


                    //street
                    dyCollection.text = syncOfflineData.offlineDumpCount.toString()
                    ssCollectionLbl.visibility = View.VISIBLE
                    ssCollection.visibility = View.VISIBLE
                    ssCollection.text = syncOfflineData.streetCount.toString()


                    //liquid
                    dyCollection.text = syncOfflineData.offlineDumpCount.toString()
                    lwcCollection.visibility = View.VISIBLE
                    lwcCollectionLbl.visibility = View.VISIBLE
                    lwcCollection.text = syncOfflineData.liquidCount.toString()

                    //master-id
                    dyCollection.text = syncOfflineData.offlineDumpCount.toString()
                    masterPlateCollectionLbl.visibility = View.VISIBLE
                    masterPlateCollection.visibility = View.VISIBLE
                    masterPlateCollection.text = syncOfflineData.masterPlateCollection.toString()
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GCViewHolder {
        val binding =
            EachGcLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GCViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GCViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem, empType)
        if (historyClickListener != null) {
            holder.itemView.setOnClickListener {
                historyClickListener?.onHistoryCardClicked(currentItem.date)
            }
        }
//        if (syncOfflineClickListener != null){
//            holder.itemView.setOnClickListener {
//                syncOfflineClickListener?.onSyncOfflineCardClicked(currentItem.hu)
//            }
//        }
    }
}

private class DiffCallback : DiffUtil.ItemCallback<SyncOfflineData>() {

    override fun areItemsTheSame(
        oldItem: SyncOfflineData,
        newItem: SyncOfflineData
    ) = oldItem.date == newItem.date


    override fun areContentsTheSame(
        oldItem: SyncOfflineData,
        newItem: SyncOfflineData
    ) = oldItem == newItem

}


interface HistoryClickListener {
    fun onHistoryCardClicked(date: String)
}