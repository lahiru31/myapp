package com.example.myshop.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myshop.R
import com.example.myshop.databinding.FragmentCartBinding
import com.example.myshop.models.CartItem
import com.example.myshop.ui.adapters.CartAdapter
import com.example.myshop.utils.FirebaseHelper
import java.text.NumberFormat
import java.util.Locale

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    
    private val firebaseHelper = FirebaseHelper.getInstance()
    private lateinit var cartAdapter: CartAdapter
    private var cartItems = mutableListOf<CartItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupButtons()
        loadCartItems()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onQuantityChanged = { cartItem, newQuantity ->
                updateCartItemQuantity(cartItem, newQuantity)
            },
            onRemoveClick = { cartItem ->
                removeCartItem(cartItem)
            }
        )

        binding.cartRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cartAdapter
        }
    }

    private fun setupButtons() {
        binding.checkoutButton.setOnClickListener {
            if (cartItems.isNotEmpty()) {
                navigateToCheckout()
            } else {
                Toast.makeText(
                    context,
                    "Your cart is empty",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.continueShoppingButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_home)
        }
    }

    private fun loadCartItems() {
        showLoading(true)
        
        val currentUser = firebaseHelper.getCurrentUser()
        if (currentUser == null) {
            showEmptyCart()
            showLoading(false)
            return
        }

        firebaseHelper.getCartItems(currentUser.uid)
            .addOnSuccessListener { querySnapshot ->
                cartItems = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(CartItem::class.java)
                }.toMutableList()
                
                if (cartItems.isEmpty()) {
                    showEmptyCart()
                } else {
                    showCartItems()
                    cartAdapter.submitList(cartItems)
                    updateTotalPrice()
                }
                showLoading(false)
            }
            .addOnFailureListener { e ->
                showError("Failed to load cart items")
                showLoading(false)
            }
    }

    private fun updateCartItemQuantity(cartItem: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeCartItem(cartItem)
            return
        }

        val currentUser = firebaseHelper.getCurrentUser() ?: return
        
        firebaseHelper.updateCartItemQuantity(currentUser.uid, cartItem.productId, newQuantity)
            .addOnSuccessListener {
                val index = cartItems.indexOfFirst { it.productId == cartItem.productId }
                if (index != -1) {
                    cartItems[index].setQuantity(newQuantity)
                    cartAdapter.notifyItemChanged(index)
                    updateTotalPrice()
                }
            }
            .addOnFailureListener { e ->
                showError("Failed to update quantity")
            }
    }

    private fun removeCartItem(cartItem: CartItem) {
        val currentUser = firebaseHelper.getCurrentUser() ?: return
        
        firebaseHelper.removeFromCart(currentUser.uid, cartItem.productId)
            .addOnSuccessListener {
                val index = cartItems.indexOfFirst { it.productId == cartItem.productId }
                if (index != -1) {
                    cartItems.removeAt(index)
                    cartAdapter.submitList(cartItems.toList())
                    
                    if (cartItems.isEmpty()) {
                        showEmptyCart()
                    }
                    
                    updateTotalPrice()
                }
            }
            .addOnFailureListener { e ->
                showError("Failed to remove item from cart")
            }
    }

    private fun updateTotalPrice() {
        val total = cartItems.sumOf { it.getTotalPrice() }
        val formattedTotal = NumberFormat.getCurrencyInstance(Locale.US).format(total)
        binding.totalPriceText.text = getString(R.string.text_cart_total, formattedTotal)
    }

    private fun showCartItems() {
        binding.apply {
            emptyCartLayout.visibility = View.GONE
            cartContent.visibility = View.VISIBLE
        }
    }

    private fun showEmptyCart() {
        binding.apply {
            cartContent.visibility = View.GONE
            emptyCartLayout.visibility = View.VISIBLE
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToCheckout() {
        val currentUser = firebaseHelper.getCurrentUser()
        if (currentUser == null) {
            showError("Please sign in to proceed")
            return
        }
        
        if (cartItems.isEmpty()) {
            showError("Your cart is empty")
            return
        }
        
        findNavController().navigate(
            CartFragmentDirections.actionCartToCheckout(
                cartItems.toTypedArray()
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
