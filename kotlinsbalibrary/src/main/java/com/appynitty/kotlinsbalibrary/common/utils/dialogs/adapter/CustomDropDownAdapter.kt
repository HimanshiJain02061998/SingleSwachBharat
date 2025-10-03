package com.appynitty.kotlinsbalibrary.common.utils.dialogs.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import androidx.annotation.LayoutRes
import java.util.Locale

class CustomDropDownAdapter(
    context: Context,
    @LayoutRes private val layoutResource: Int,
    private val mList: List<String>
) :
    ArrayAdapter<String>(context, layoutResource, mList),
    Filterable {

    private var currentList: List<String> = mList

    override fun getCount(): Int {
        return currentList.size
    }

    override fun getItem(p0: Int): String? {
        return currentList[p0]
    }

    override fun getItemId(p0: Int): Long {
        // Or just return p0
        return p0.toLong()
    }

//    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//        val view: TextView = convertView as TextView? ?: LayoutInflater.from(context).inflate(layoutResource, parent, false) as TextView
//        view.text = mList[position]
//        return view
//    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return super.getView(position, convertView, parent)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(
                charSequence: CharSequence?,
                filterResults: Filter.FilterResults
            ) {
                currentList = filterResults.values as List<String>
                notifyDataSetChanged()
            }

            override fun performFiltering(charSequence: CharSequence?): Filter.FilterResults {
                val queryString = charSequence?.toString()?.lowercase(Locale.ROOT)

                val filterResults = FilterResults()
                filterResults.values = if (queryString.isNullOrEmpty())
                    mList
                else
                    currentList.filter {
                        it.lowercase(Locale.ROOT).contains(queryString)
                    }
                return filterResults
            }
        }
    }
}