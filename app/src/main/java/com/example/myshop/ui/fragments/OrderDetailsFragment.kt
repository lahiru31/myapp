package com.example.myshop.ui.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myshop.R
import com.example.myshop.databinding.FragmentOrderDetailsBinding
import com.example.myshop.models.Order
import com.example.myshop.ui.adapters.CartAdapter
import com.example.myshop.utils.Constants
import com.example.myshop.utils.FirebaseHelper
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class OrderDetailsFragment : Fragment() {

    private var _binding: FragmentOrderDetailsBinding? = null
    private val binding get() = _binding!!
    
    private val args: OrderDetailsFragmentArgs by navArgs()
    private val firebaseHelper = FirebaseHelper.getInstance()
    private lateinit var cartAdapter: CartAdapter
    private var currentOrder: Order? = null

    private val orderUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Constants.ACTION_ORDER_UPDATED) {
                val orderId = intent.getStringExtra(Constants.EXTRA_ORDER_ID)
                if (orderId == args.orderId) {
                    loadOrderDetails()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadOrderDetails()

        // Register broadcast receiver for order updates
        requireContext().registerReceiver(
            orderUpdateReceiver,
            IntentFilter(Constants.ACTION_ORDER_UPDATED)
        )
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onQuantityChanged = { _, _ -> }, // Read-only in order details
            onRemoveClick = { _ -> } // Read-only in order details
        )

        binding.orderItemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cartAdapter
        }
    }

    private fun loadOrderDetails() {
        showLoading(true)

        firebaseHelper.getOrderById(args.orderId)
            .addOnSuccessListener { documentSnapshot ->
                val order = documentSnapshot.toObject(Order::class.java)
                if (order != null) {
                    currentOrder = order
                    updateUI(order)
                } else {
                    showError("Order not found")
                }
                showLoading(false)
            }
            .addOnFailureListener { e ->
                showError("Failed to load order details")
                showLoading(false)
            }
    }

    private fun updateUI(order: Order) {
        val formatter = NumberFormat.getCurrencyInstance(Locale.US)
        val dateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

        binding.apply {
            // Update order status with appropriate color
            orderStatusText.text = order.status
            orderStatusText.setTextColor(getStatusColor(order.status))
            
            // Update order date
            orderDateText.text = dateFormatter.format(order.orderDate)

            // Update order items
            cartAdapter.submitList(order.items)

            // Update shipping address
            shippingAddressText.text = order.shippingAddress

            // Update price details
            subtotalText.text = formatter.format(order.subtotal)
            shippingText.text = formatter.format(order.shippingCost)
            totalText.text = formatter.format(order.total)
        }
    }

    private fun getStatusColor(status: String): Int {
        return when (status) {
            Constants.ORDER_STATUS_PENDING -> requireContext().getColor(R.color.status_pending)
            Constants.ORDER_STATUS_CONFIRMED -> requireContext().getColor(R.color.status_confirmed)
            Constants.ORDER_STATUS_SHIPPED -> requireContext().getColor(R.color.status_shipped)
            Constants.ORDER_STATUS_DELIVERED -> requireContext().getColor(R.color.status_delivered)
            Constants.ORDER_STATUS_CANCELLED -> requireContext().getColor(R.color.status_cancelled)
            else -> requireContext().getColor(R.color.black)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            loadingProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
            orderContent.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            // Unregister broadcast receiver
            requireContext().unregisterReceiver(orderUpdateReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered
        }
        _binding = null
    }
}
