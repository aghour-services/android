package com.aghourservices.ui.viewHolders

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.aghourservices.R
import com.aghourservices.data.model.Category
import com.aghourservices.databinding.SpinnerItemsBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class SpinnerCategoriesAdapter(context: Context, category: List<Category>) :
    ArrayAdapter<Category>(context, 0, category) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, parent)
    }

    private fun initView(position: Int, parent: ViewGroup): View {
        val category = getItem(position)
        val binding = SpinnerItemsBinding.inflate(LayoutInflater.from(context), parent, false)
        val categoryImage = binding.categoryImage
        val categoryName = binding.categoryName

        Glide.with(context)
            .load(category?.icon)
            .placeholder(R.drawable.ic_download)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(categoryImage)
        categoryName.text = category?.name

        return binding.root
    }
}
