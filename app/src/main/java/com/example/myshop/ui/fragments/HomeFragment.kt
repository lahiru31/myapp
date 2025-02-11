package com.example.myshop.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myshop.R
import com.example.myshop.databinding.FragmentHomeBinding
import com.example.myshop.models.Product
import com.example.myshop.ui.adapters.ProductAdapter
import com.example.myshop.ui.adapters.CategoryAdapter
import com.example.myshop.utils.FirebaseHelper
import com.google.firebase.firestore.QuerySnapshot

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val firebaseHelper = FirebaseHelper.getInstance()
    private lateinit var productAdapter: ProductAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerViews()
        setupSwipeRefresh()
        loadData()
    }

    private fun setupRecyclerViews() {
        // Setup Featured Products RecyclerView
        productAdapter = ProductAdapter { product ->
            navigateToProductDetail(product)
        }
        binding.featuredProductsRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = productAdapter
        }

        // Setup Categories RecyclerView
        categoryAdapter = CategoryAdapter { category ->
            navigateToCategoryProducts(category)
        }
        binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadData()
        }
    }

    private fun loadData() {
        binding.swipeRefreshLayout.isRefreshing = true

        // Load featured products
        firebaseHelper.getAllProducts()
            .addOnSuccessListener { querySnapshot ->
                handleProductsResponse(querySnapshot)
                binding.swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener { e ->
                handleError(e)
                binding.swipeRefreshLayout.isRefreshing = false
            }

        // Load categories (you might want to create a separate collection for categories in Firebase)
        loadCategories()
    }

    private fun handleProductsResponse(querySnapshot: QuerySnapshot) {
        val products = querySnapshot.documents.mapNotNull { document ->
            document.toObject(Product::class.java)
        }
        
        if (products.isEmpty()) {
            binding.noProductsText.visibility = View.VISIBLE
            binding.featuredProductsRecyclerView.visibility = View.GONE
        } else {
            binding.noProductsText.visibility = View.GONE
            binding.featuredProductsRecyclerView.visibility = View.VISIBLE
            productAdapter.submitList(products)
        }
    }

    private fun loadCategories() {
        // For now, using static categories. In a real app, these would come from Firebase
        val categories = listOf(
            "Electronics",
            "Fashion",
            "Home & Living",
            "Books",
            "Sports",
            "Beauty",
            "Toys",
            "Automotive"
        )
        categoryAdapter.submitList(categories)
    }

    private fun navigateToProductDetail(product: Product) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeToProductDetail(product.id)
        )
    }

    private fun navigateToCategoryProducts(category: String) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeToCategoryProducts(category)
        )
    }

    private fun handleError(exception: Exception) {
        Toast.makeText(
            context,
            getString(R.string.error_network),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}
