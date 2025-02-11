package com.example.myshop.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myshop.R
import com.example.myshop.databinding.FragmentAddressListBinding
import com.example.myshop.models.Address
import com.example.myshop.ui.adapters.AddressAdapter
import com.example.myshop.ui.viewmodels.AddressViewModel
import com.example.myshop.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AddressListFragment : Fragment() {

    private var _binding: FragmentAddressListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddressViewModel by viewModels()
    private lateinit var addressAdapter: AddressAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddressListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        addressAdapter = AddressAdapter(
            onEditClick = { address ->
                findNavController().navigate(
                    AddressListFragmentDirections.actionAddressListToAddAddress(address)
                )
            },
            onDeleteClick = { address ->
                showDeleteConfirmationDialog(address)
            },
            onSetDefaultClick = { address ->
                viewModel.setDefaultAddress(address.id)
            }
        )

        binding.addressRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = addressAdapter
        }
    }

    private fun setupObservers() {
        viewModel.getUserAddresses()?.observe(viewLifecycleOwner) { addresses ->
            addressAdapter.submitList(addresses)
            updateEmptyState(addresses.isEmpty())
        }

        viewModel.operationStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    showLoading(false)
                }
                is Resource.Error -> {
                    showLoading(false)
                    showError(resource.message ?: getString(R.string.error_unknown))
                }
                is Resource.Loading -> {
                    showLoading(true)
                }
            }
        }
    }

    private fun setupListeners() {
        binding.addAddressButton.setOnClickListener {
            findNavController().navigate(
                AddressListFragmentDirections.actionAddressListToAddAddress(null)
            )
        }
    }

    private fun showDeleteConfirmationDialog(address: Address) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_delete_address)
            .setMessage(R.string.message_delete_address_confirmation)
            .setPositiveButton(R.string.btn_delete) { _, _ ->
                viewModel.deleteAddress(address)
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.apply {
            if (isEmpty) {
                addressRecyclerView.visibility = View.GONE
                emptyAddressLayout.visibility = View.VISIBLE
            } else {
                addressRecyclerView.visibility = View.VISIBLE
                emptyAddressLayout.visibility = View.GONE
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            loadingProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
            addressContent.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
