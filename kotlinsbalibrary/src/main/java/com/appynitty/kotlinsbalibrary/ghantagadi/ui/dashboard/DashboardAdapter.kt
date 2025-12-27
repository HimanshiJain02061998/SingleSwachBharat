package com.appynitty.kotlinsbalibrary.ghantagadi.ui.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.databinding.DashboarEachItemBinding

class DashboardAdapter(private val mList: List<DashboardMenu>,val context: Context) :
    RecyclerView.Adapter<DashboardAdapter.DashboardViewHolder>() {

    //listener variable and setter
    private var menuItemClickedInterface: MenuItemClickedInterface? = null
    fun setListener(menuItemClickedInterface: MenuItemClickedInterface) {
        this.menuItemClickedInterface = menuItemClickedInterface
    }

    private var isClickable = true

    fun setClickable(isClickable: Boolean) {
        this.isClickable = isClickable
    }

    class DashboardViewHolder(val binding: DashboarEachItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardViewHolder {
        val binding =
            DashboarEachItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DashboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DashboardViewHolder, position: Int) {


        with(holder) {

            this.itemView.isEnabled = isClickable

            with(mList[position]) {
                binding.menuIcon.setImageResource(this.menuImage)
                binding.menuTitle.text = this.menuName

                    if (mList[position].archivedCount>0){
                        binding.badgeText.visibility = View.VISIBLE
                        binding.badgeText.text = mList[position].archivedCount.toString()
                    }else{
                        binding.badgeText.visibility = View.GONE
                    }


                holder.itemView.setOnClickListener {
                    menuItemClickedInterface?.onMenuItemClicked(this)
                }
            }
        }


    }

    override fun getItemCount(): Int {
        return mList.size
    }

    interface MenuItemClickedInterface {
        fun onMenuItemClicked(menuItem: DashboardMenu)
    }

}