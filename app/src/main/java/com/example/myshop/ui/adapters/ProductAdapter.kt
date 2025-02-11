package com.example.myshop.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myshop.R
import com.example.myshop.databinding.ItemProductBinding
import com.example.myshop.models.Product
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private val onProductClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding, onProductClick)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ProductViewHolder(
        private val binding: ItemProductBinding,
        private val onProductClick: (Product) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            with(binding) {
                // Set product name
                productName.text = product.name

                // Format and set price
                val formattedPrice = NumberFormat.getCurrencyInstance(Locale.US)
                    .format(product.price)
                productPrice.text = formattedPrice

                // Load product image using Glide
                Glide.with(productImage)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.error_product)
                    .centerCrop()
                    .into(productImage)

                // Set stock status
                if (product.stockQuantity > 0) {
                    stockStatus.setTextColor(root.context.getColor(R.color.success))
                    stockStatus.text = root.context.getString(R.string.text_in_stock)
                } else {
                    stockStatus.setTextColor(root.context.getColor(R.color.error))
                    stockStatus.text = root.context.getString(R.string.text_out_of_stock)
                }

                // Set click listener
                root.setOnClickListener { onProductClick(product) }
            }
        }
    }

    private class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}
