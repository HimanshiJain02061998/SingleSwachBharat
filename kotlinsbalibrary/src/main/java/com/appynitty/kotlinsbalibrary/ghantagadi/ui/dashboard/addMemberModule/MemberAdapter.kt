package com.appynitty.kotlinsbalibrary.ghantagadi.ui.dashboard.addMemberModule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.AvailableEmpItem

class MemberAdapter(private var employeeList: MutableList<AvailableEmpItem>) :
    RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    private var filteredList: MutableList<AvailableEmpItem> = employeeList.toMutableList()
    private val memberUserIds = mutableSetOf<Int>()
    var onSelectionChanged: ((Int) -> Unit)? = null
    inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBoxMember)
        val tvName: TextView = itemView.findViewById(R.id.tvMemberName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_member_checkbox, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = filteredList[position]
        holder.tvName.text = member.EmployeeName
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = memberUserIds.contains(member.userid)

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) memberUserIds.add(member.userid)
            else memberUserIds.remove(member.userid)
        }
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) memberUserIds.add(member.userid)
            else memberUserIds.remove(member.userid)
            // 🔹 Notify activity about the new selection count
            onSelectionChanged?.invoke(memberUserIds.size)
        }

        holder.itemView.setOnClickListener {
            holder.checkBox.isChecked = !holder.checkBox.isChecked
        }
    }

    override fun getItemCount(): Int = filteredList.size

    fun updateList(newList: List<AvailableEmpItem>) {
        employeeList.clear()
        employeeList.addAll(newList)
        filteredList.clear()
        filteredList.addAll(newList)
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            employeeList.toMutableList()
        } else {
            employeeList.filter {
                it.EmployeeName.contains(query, ignoreCase = true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    fun getSelectedMembers(): List<AvailableEmpItem> {
        return employeeList.filter { memberUserIds.contains(it.userid) }
    }
}
