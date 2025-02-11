package com.example.myshop.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myshop.data.AppDatabase
import com.example.myshop.data.repository.AddressRepository
import com.example.myshop.models.Address
import com.example.myshop.utils.Resource
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddressViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AddressRepository
    private val currentUserId: String? = null // TODO: Get from FirebaseAuth

    private val _selectedAddress = MutableLiveData<Address?>()
    val selectedAddress: LiveData<Address?> = _selectedAddress

    private val _operationStatus = MutableLiveData<Resource<Unit>>()
    val operationStatus: LiveData<Resource<Unit>> = _operationStatus

    private val _placePredictions = MutableStateFlow<Resource<List<Place.AutocompletePrediction>>>(Resource.Success(emptyList()))
    val placePredictions: StateFlow<Resource<List<Place.AutocompletePrediction>>> = _placePredictions

    private val _selectedLocation = MutableLiveData<LatLng>()
    val selectedLocation: LiveData<LatLng> = _selectedLocation

    init {
        val addressDao = AppDatabase.getInstance(application).addressDao()
        repository = AddressRepository(addressDao, application)
    }

    fun getUserAddresses(): LiveData<List<Address>>? {
        return currentUserId?.let { userId ->
            repository.getAddresses(userId)
        }
    }

    fun getDefaultAddress(): LiveData<Address?>? {
        return currentUserId?.let { userId ->
            repository.getDefaultAddress(userId)
        }
    }

    fun addAddress(address: Address) {
        viewModelScope.launch {
            currentUserId?.let { userId ->
                val addressWithUserId = address.copy(userId = userId)
                val result = repository.addAddress(addressWithUserId)
                _operationStatus.value = when (result) {
                    is Resource.Success -> Resource.Success(Unit)
                    is Resource.Error -> Resource.Error(result.message ?: "Failed to add address")
                    is Resource.Loading -> Resource.Loading()
                }
            }
        }
    }

    fun updateAddress(address: Address) {
        viewModelScope.launch {
            val result = repository.updateAddress(address)
            _operationStatus.value = result
        }
    }

    fun deleteAddress(address: Address) {
        viewModelScope.launch {
            val result = repository.deleteAddress(address)
            _operationStatus.value = result
        }
    }

    fun setDefaultAddress(addressId: Long) {
        viewModelScope.launch {
            currentUserId?.let { userId ->
                val result = repository.setDefaultAddress(userId, addressId)
                _operationStatus.value = result
            }
        }
    }

    fun searchPlaces(query: String) {
        if (query.length < 3) {
            _placePredictions.value = Resource.Success(emptyList())
            return
        }

        viewModelScope.launch {
            repository.getPlacePredictions(query).collect { result ->
                _placePredictions.value = result
            }
        }
    }

    fun getPlaceDetails(placeId: String) {
        viewModelScope.launch {
            when (val result = repository.getPlaceDetails(placeId)) {
                is Resource.Success -> {
                    result.data?.latLng?.let { latLng ->
                        _selectedLocation.value = latLng
                        getAddressFromLocation(latLng)
                    }
                }
                is Resource.Error -> _operationStatus.value = Resource.Error(result.message ?: "Failed to get place details")
                is Resource.Loading -> _operationStatus.value = Resource.Loading()
            }
        }
    }

    fun getAddressFromLocation(latLng: LatLng) {
        viewModelScope.launch {
            when (val result = repository.getAddressFromLocation(latLng)) {
                is Resource.Success -> _selectedAddress.value = result.data
                is Resource.Error -> _operationStatus.value = Resource.Error(result.message ?: "Failed to get address")
                is Resource.Loading -> _operationStatus.value = Resource.Loading()
            }
        }
    }

    fun getLocationFromAddress(address: String) {
        viewModelScope.launch {
            when (val result = repository.getLocationFromAddress(address)) {
                is Resource.Success -> result.data?.let { _selectedLocation.value = it }
                is Resource.Error -> _operationStatus.value = Resource.Error(result.message ?: "Failed to get location")
                is Resource.Loading -> _operationStatus.value = Resource.Loading()
            }
        }
    }

    fun setSelectedAddress(address: Address) {
        _selectedAddress.value = address
    }

    fun clearSelectedAddress() {
        _selectedAddress.value = null
    }

    fun clearSelectedLocation() {
        _selectedLocation.value = null
    }
}
