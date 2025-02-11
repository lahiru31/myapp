package com.example.myshop.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myshop.R
import com.example.myshop.databinding.FragmentAddAddressBinding
import com.example.myshop.models.Address
import com.example.myshop.ui.viewmodels.AddressViewModel
import com.example.myshop.utils.Resource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.android.widget.textChanges

class AddAddressFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentAddAddressBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddressViewModel by viewModels()
    private val args: AddAddressFragmentArgs by navArgs()

    private var googleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMap()
        setupLocationServices()
        setupSearchBar()
        setupObservers()
        setupListeners()

        // If editing existing address, populate fields
        args.address?.let { address ->
            populateFields(address)
        }
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (hasLocationPermission()) {
            getCurrentLocation()
        }
    }

    private fun setupSearchBar() {
        binding.searchInput.textChanges()
            .debounce(500)
            .onEach { text ->
                if (!text.isNullOrBlank()) {
                    viewModel.searchPlaces(text.toString())
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun setupObservers() {
        viewModel.selectedLocation.observe(viewLifecycleOwner) { latLng ->
            updateMapLocation(latLng)
        }

        viewModel.selectedAddress.observe(viewLifecycleOwner) { address ->
            address?.let { populateAddressFields(it) }
        }

        viewModel.operationStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    showLoading(false)
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    showLoading(false)
                    showError(resource.message ?: getString(R.string.error_unknown))
                }
                is Resource.Loading -> showLoading(true)
            }
        }
    }

    private fun setupListeners() {
        binding.saveButton.setOnClickListener {
            if (validateInputs()) {
                saveAddress()
            }
        }
    }

    private fun populateFields(address: Address) {
        binding.apply {
            nameInput.setText(address.name)
            phoneInput.setText(address.phoneNumber)
            addressLine1Input.setText(address.addressLine1)
            addressLine2Input.setText(address.addressLine2)
            cityInput.setText(address.city)
            zipCodeInput.setText(address.zipCode)
        }

        // Update map location
        updateMapLocation(LatLng(address.latitude, address.longitude))
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        binding.apply {
            if (nameInput.text.toString().trim().isEmpty()) {
                nameLayout.error = getString(R.string.error_required_field)
                isValid = false
            }
            if (phoneInput.text.toString().trim().isEmpty()) {
                phoneLayout.error = getString(R.string.error_required_field)
                isValid = false
            }
            if (addressLine1Input.text.toString().trim().isEmpty()) {
                addressLine1Layout.error = getString(R.string.error_required_field)
                isValid = false
            }
            if (cityInput.text.toString().trim().isEmpty()) {
                cityLayout.error = getString(R.string.error_required_field)
                isValid = false
            }
            if (zipCodeInput.text.toString().trim().isEmpty()) {
                zipCodeLayout.error = getString(R.string.error_required_field)
                isValid = false
            }
        }
        return isValid
    }

    private fun saveAddress() {
        val address = Address(
            id = args.address?.id ?: 0,
            name = binding.nameInput.text.toString().trim(),
            phoneNumber = binding.phoneInput.text.toString().trim(),
            addressLine1 = binding.addressLine1Input.text.toString().trim(),
            addressLine2 = binding.addressLine2Input.text.toString().trim(),
            city = binding.cityInput.text.toString().trim(),
            zipCode = binding.zipCodeInput.text.toString().trim(),
            latitude = currentLocation?.latitude ?: 0.0,
            longitude = currentLocation?.longitude ?: 0.0
        )

        if (args.address == null) {
            viewModel.addAddress(address)
        } else {
            viewModel.updateAddress(address)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.setOnMapClickListener { latLng ->
            currentLocation = latLng
            updateMapLocation(latLng)
            viewModel.getAddressFromLocation(latLng)
        }

        // If editing, show existing location
        args.address?.let { address ->
            updateMapLocation(LatLng(address.latitude, address.longitude))
        }
    }

    private fun updateMapLocation(latLng: LatLng) {
        googleMap?.apply {
            clear()
            addMarker(MarkerOptions().position(latLng))
            animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
        currentLocation = latLng
    }

    private fun getCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    currentLocation = latLng
                    updateMapLocation(latLng)
                    viewModel.getAddressFromLocation(latLng)
                }
            }
        } catch (e: SecurityException) {
            showError(getString(R.string.error_location_permission))
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
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
