package com.example.myshop.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myshop.databinding.ItemAddressBinding
import com.example.myshop.models.Address

class AddressAdapter(
    private val onEditClick: (Address) -> Unit,
    private val onDeleteClick: (Address) -> Unit,
    private val onSetDefaultClick: (Address) -> Unit,
    private val onAddressClick: ((Address) -> Unit)? = null
) : ListAdapter<Address, AddressAdapter.AddressViewHolder>(AddressDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val binding = ItemAddressBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AddressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AddressViewHolder(
        private val binding: ItemAddressBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onAddressClick?.invoke(getItem(position))
                }
            }

            binding.editButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEditClick(getItem(position))
                }
            }

            binding.deleteButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(getItem(position))
                }
            }

            binding.setDefaultButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSetDefaultClick(getItem(position))
                }
            }
        }

        fun bind(address: Address) {
            binding.apply {
                nameText.text = address.name
                phoneText.text = address.phoneNumber
                addressText.text = address.getFullAddress()

                // Show/hide default badge and set default button
                defaultBadge.visibility = if (address.isDefault) View.VISIBLE else View.GONE
                setDefaultButton.visibility = if (address.isDefault) View.GONE else View.VISIBLE

                // If this is being used in selection mode (e.g., during checkout)
                if (onAddressClick != null) {
                    // Hide edit/delete/set default buttons in selection mode
                    editButton.visibility = View.GONE
                    deleteButton.visibility = View.GONE
                    setDefaultButton.visibility = View.GONE
                    // Make the whole card clickable
                    root.isClickable = true
                    root.isFocusable = true
                } else {
                    // Show management buttons in normal mode
                    editButton.visibility = View.VISIBLE
                    deleteButton.visibility = View.VISIBLE
                    setDefaultButton.visibility = if (address.isDefault) View.GONE else View.VISIBLE
                    // Disable card click in management mode
                    root.isClickable = false
                    root.isFocusable = false
                }
            }
        }
    }

    private class AddressDiffCallback : DiffUtil.ItemCallback<Address>() {
        override fun areItemsTheSame(oldItem: Address, newItem: Address): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Address, newItem: Address): Boolean {
            return oldItem == newItem
        }
    }
}
