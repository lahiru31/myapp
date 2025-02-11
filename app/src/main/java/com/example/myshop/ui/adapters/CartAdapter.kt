package com.example.myshop.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myshop.R
import com.example.myshop.databinding.ItemCartBinding
import com.example.myshop.models.CartItem
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val onQuantityChanged: (CartItem, Int) -> Unit,
    private val onRemoveClick: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding, onQuantityChanged, onRemoveClick)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CartViewHolder(
        private val binding: ItemCartBinding,
        private val onQuantityChanged: (CartItem, Int) -> Unit,
        private val onRemoveClick: (CartItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItem) {
            with(binding) {
                // Set product name
                productName.text = cartItem.productName

                // Format and set price
                val formattedPrice = NumberFormat.getCurrencyInstance(Locale.US)
                    .format(cartItem.productPrice)
                productPrice.text = formattedPrice

                // Set total price
                val formattedTotal = NumberFormat.getCurrencyInstance(Locale.US)
                    .format(cartItem.totalPrice)
                totalPrice.text = formattedTotal

                // Set quantity
                quantityText.text = cartItem.quantity.toString()

                // Load product image
                Glide.with(productImage)
                    .load(cartItem.productImage)
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.error_product)
                    .centerCrop()
                    .into(productImage)

                // Setup quantity controls
                decreaseButton.setOnClickListener {
                    if (cartItem.quantity > 1) {
                        onQuantityChanged(cartItem, cartItem.quantity - 1)
                    }
                }

                increaseButton.setOnClickListener {
                    onQuantityChanged(cartItem, cartItem.quantity + 1)
                }

                // Setup remove button
                removeButton.setOnClickListener {
                    onRemoveClick(cartItem)
                }

                // Update decrease button state
                decreaseButton.isEnabled = cartItem.quantity > 1
            }
        }
    }

    private class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.quantity == newItem.quantity &&
                    oldItem.totalPrice == newItem.totalPrice
        }
    }
}
