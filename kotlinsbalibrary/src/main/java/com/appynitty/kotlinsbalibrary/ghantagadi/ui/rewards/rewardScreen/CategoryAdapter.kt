package com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.rewardScreen

import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.appynitty.kotlinsbalibrary.databinding.ItemCategoryBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.Category
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

class CategoryAdapter(
    private var categories: List<Category>,
    private val onClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>()  {

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.tvHeading.text = category.catName

            Glide.with(binding.root.context)
                .load(category.catLogoUrl)
                .into(binding.ivMain)

            Glide.with(binding.root.context)
                .load(category.smallLogoUrl)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        val sizeInDp = 12
                        val sizeInPx = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            sizeInDp.toFloat(),
                            binding.root.context.resources.displayMetrics
                        ).toInt()

                        resource.setBounds(0, 0, sizeInPx, sizeInPx)
                        binding.tvHeading.setCompoundDrawables(resource, null, null, null)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })

            binding.root.setOnClickListener { onClick(category) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding =
            ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    fun updateData(newCategories: List<Category>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = categories.size
            override fun getNewListSize() = newCategories.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return categories[oldItemPosition].catId == newCategories[newItemPosition].catId
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return categories[oldItemPosition] == newCategories[newItemPosition]
            }
        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)
        categories = newCategories
        diffResult.dispatchUpdatesTo(this)
    }
}