package com.example.myshop.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myshop.R
import com.example.myshop.databinding.FragmentOrderConfirmationBinding
import com.example.myshop.utils.FirebaseHelper

class OrderConfirmationFragment : Fragment() {

    private var _binding: FragmentOrderConfirmationBinding? = null
    private val binding get() = _binding!!
    
    private val args: OrderConfirmationFragmentArgs by navArgs()
    private val firebaseHelper = FirebaseHelper.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupButtons()
    }

    private fun setupUI() {
        binding.orderIdText.text = args.orderId
    }

    private fun setupButtons() {
        binding.apply {
            continueShoppingButton.setOnClickListener {
                // Clear the back stack and navigate to home
                findNavController().navigate(
                    R.id.action_global_home,
                    null,
                    navOptions {
                        popUpTo(R.id.nav_graph) {
                            inclusive = true
                        }
                    }
                )
            }

            viewOrderButton.setOnClickListener {
                // Navigate to order details
                findNavController().navigate(
                    OrderConfirmationFragmentDirections.actionOrderConfirmationToOrderDetails(args.orderId)
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
