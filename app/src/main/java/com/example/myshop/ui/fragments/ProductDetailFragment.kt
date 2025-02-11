package com.example.myshop.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.myshop.R
import com.example.myshop.databinding.FragmentProductDetailBinding
import com.example.myshop.models.CartItem
import com.example.myshop.models.Product
import com.example.myshop.utils.FirebaseHelper
import java.text.NumberFormat
import java.util.Locale

class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    
    private val args: ProductDetailFragmentArgs by navArgs()
    private val firebaseHelper = FirebaseHelper.getInstance()
    private var currentProduct: Product? = null
    private var currentQuantity = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupQuantityControls()
        loadProductDetails()
        setupButtons()
    }

    private fun setupQuantityControls() {
        binding.apply {
            decreaseButton.setOnClickListener {
                if (currentQuantity > 1) {
                    currentQuantity--
                    updateQuantityUI()
                }
            }

            increaseButton.setOnClickListener {
                if (currentQuantity < (currentProduct?.stockQuantity ?: 0)) {
                    currentQuantity++
                    updateQuantityUI()
                }
            }
        }
    }

    private fun loadProductDetails() {
        showLoading(true)
        
        firebaseHelper.getProductById(args.productId)
            .addOnSuccessListener { product ->
                if (product != null) {
                    currentProduct = product
                    updateUI(product)
                } else {
                    showError("Product not found")
                    findNavController().navigateUp()
                }
                showLoading(false)
            }
            .addOnFailureListener { e ->
                showError("Failed to load product details")
                showLoading(false)
                findNavController().navigateUp()
            }
    }

    private fun updateUI(product: Product) {
        binding.apply {
            // Load product image
            Glide.with(productImage)
                .load(product.imageUrl)
                .placeholder(R.drawable.placeholder_product)
                .error(R.drawable.error_product)
                .into(productImage)

            // Set product details
            productName.text = product.name
            productDescription.text = product.description
            
            // Format and set price
            val formattedPrice = NumberFormat.getCurrencyInstance(Locale.US)
                .format(product.price)
            productPrice.text = formattedPrice

            // Update stock status
            if (product.stockQuantity > 0) {
                stockStatus.setTextColor(requireContext().getColor(R.color.success))
                stockStatus.text = getString(R.string.text_in_stock)
                addToCartButton.isEnabled = true
                buyNowButton.isEnabled = true
            } else {
                stockStatus.setTextColor(requireContext().getColor(R.color.error))
                stockStatus.text = getString(R.string.text_out_of_stock)
                addToCartButton.isEnabled = false
                buyNowButton.isEnabled = false
            }

            // Update quantity controls
            updateQuantityUI()
        }
    }

    private fun updateQuantityUI() {
        binding.apply {
            quantityText.text = currentQuantity.toString()
            decreaseButton.isEnabled = currentQuantity > 1
            increaseButton.isEnabled = currentQuantity < (currentProduct?.stockQuantity ?: 0)

            // Update total price
            currentProduct?.let { product ->
                val total = product.price * currentQuantity
                val formattedTotal = NumberFormat.getCurrencyInstance(Locale.US)
                    .format(total)
                totalPrice.text = getString(R.string.label_price, formattedTotal)
            }
        }
    }

    private fun setupButtons() {
        binding.apply {
            addToCartButton.setOnClickListener {
                addToCart()
            }

            buyNowButton.setOnClickListener {
                addToCart(true)
            }
        }
    }

    private fun addToCart(proceedToCheckout: Boolean = false) {
        val product = currentProduct ?: return
        val currentUser = firebaseHelper.getCurrentUser()

        if (currentUser == null) {
            showError("Please login to add items to cart")
            return
        }

        showLoading(true)
        
        val cartItem = CartItem(
            productId = product.id,
            productName = product.name,
            productPrice = product.price,
            productImage = product.imageUrl,
            quantity = currentQuantity
        )

        firebaseHelper.addToCart(currentUser.uid, cartItem)
            .addOnSuccessListener {
                if (proceedToCheckout) {
                    findNavController().navigate(
                        ProductDetailFragmentDirections.actionProductDetailToCart()
                    )
                } else {
                    showSuccess("Item added to cart")
                }
                showLoading(false)
            }
            .addOnFailureListener { e ->
                showError("Failed to add item to cart")
                showLoading(false)
            }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            loadingProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
            productContent.visibility = if (isLoading) View.GONE else View.VISIBLE
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
