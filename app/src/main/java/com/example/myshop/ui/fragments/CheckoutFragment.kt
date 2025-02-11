package com.example.myshop.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myshop.R
import com.example.myshop.databinding.FragmentCheckoutBinding
import com.example.myshop.models.CartItem
import com.example.myshop.models.Order
import com.example.myshop.ui.adapters.CartAdapter
import com.example.myshop.utils.FirebaseHelper
import java.text.NumberFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!
    
    private val args: CheckoutFragmentArgs by navArgs()
    private val firebaseHelper = FirebaseHelper.getInstance()
    private lateinit var cartAdapter: CartAdapter
    private var cartItems = listOf<CartItem>()
    
    private val SHIPPING_COST = 5.99 // Fixed shipping cost for simplicity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupOrderSummary()
        setupButtons()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onQuantityChanged = { _, _ -> }, // Read-only in checkout
            onRemoveClick = { _ -> } // Read-only in checkout
        )

        binding.orderItemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cartAdapter
        }

        // Load cart items passed from CartFragment
        cartItems = args.cartItems.toList()
        cartAdapter.submitList(cartItems)
    }

    private fun setupOrderSummary() {
        val subtotal = cartItems.sumOf { it.getTotalPrice() }
        val total = subtotal + SHIPPING_COST

        val formatter = NumberFormat.getCurrencyInstance(Locale.US)
        
        binding.apply {
            subtotalText.text = formatter.format(subtotal)
            shippingText.text = formatter.format(SHIPPING_COST)
            totalText.text = formatter.format(total)
            bottomTotalText.text = getString(R.string.label_total_amount, formatter.format(total))
        }
    }

    private fun setupButtons() {
        binding.placeOrderButton.setOnClickListener {
            if (validateInputs()) {
                placeOrder()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        binding.apply {
            // Validate Address Line 1
            if (addressLine1Input.text.toString().trim().isEmpty()) {
                addressLine1Layout.error = getString(R.string.error_required_field)
                isValid = false
            } else {
                addressLine1Layout.error = null
            }

            // Validate City
            if (cityInput.text.toString().trim().isEmpty()) {
                cityLayout.error = getString(R.string.error_required_field)
                isValid = false
            } else {
                cityLayout.error = null
            }

            // Validate Zip Code
            val zipCode = zipCodeInput.text.toString().trim()
            if (zipCode.isEmpty()) {
                zipCodeLayout.error = getString(R.string.error_required_field)
                isValid = false
            } else if (zipCode.length < 5) {
                zipCodeLayout.error = getString(R.string.error_invalid_zip)
                isValid = false
            } else {
                zipCodeLayout.error = null
            }
        }
        return isValid
    }

    private fun placeOrder() {
        val currentUser = firebaseHelper.getCurrentUser()
        if (currentUser == null) {
            showError("Please sign in to place order")
            return
        }

        showLoading(true)

        val order = Order(
            orderId = UUID.randomUUID().toString(),
            userId = currentUser.uid,
            items = cartItems,
            subtotal = cartItems.sumOf { it.getTotalPrice() },
            shippingCost = SHIPPING_COST,
            total = cartItems.sumOf { it.getTotalPrice() } + SHIPPING_COST,
            shippingAddress = buildShippingAddress(),
            status = "PENDING",
            orderDate = Date()
        )

        firebaseHelper.createOrder(order)
            .addOnSuccessListener { documentRef ->
                // Clear cart after successful order
                firebaseHelper.clearCart(currentUser.uid)
                    .addOnSuccessListener {
                        showSuccess("Order placed successfully!")
                        navigateToOrderConfirmation(documentRef.id)
                    }
                    .addOnFailureListener { e ->
                        showError("Order placed but failed to clear cart")
                        navigateToOrderConfirmation(documentRef.id)
                    }
            }
            .addOnFailureListener { e ->
                showError("Failed to place order")
                showLoading(false)
            }
    }

    private fun buildShippingAddress(): String {
        return buildString {
            append(binding.addressLine1Input.text.toString().trim())
            
            val addressLine2 = binding.addressLine2Input.text.toString().trim()
            if (addressLine2.isNotEmpty()) {
                append("\n")
                append(addressLine2)
            }
            
            append("\n")
            append(binding.cityInput.text.toString().trim())
            append(" ")
            append(binding.zipCodeInput.text.toString().trim())
        }
    }

    private fun navigateToOrderConfirmation(orderId: String) {
        findNavController().navigate(
            CheckoutFragmentDirections.actionCheckoutToOrderConfirmation(orderId)
        )
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            loadingProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
            placeOrderButton.isEnabled = !isLoading
        }
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
