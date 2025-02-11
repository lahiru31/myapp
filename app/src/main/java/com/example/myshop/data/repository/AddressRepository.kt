package com.example.myshop.data.repository

import android.location.Geocoder
import android.content.Context
import androidx.lifecycle.LiveData
import com.example.myshop.data.dao.AddressDao
import com.example.myshop.models.Address
import com.example.myshop.utils.Resource
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.*

class AddressRepository(
    private val addressDao: AddressDao,
    private val context: Context
) {
    private val placesClient: PlacesClient = Places.createClient(context)

    // Room Database Operations
    fun getAddresses(userId: String): LiveData<List<Address>> = addressDao.getAddresses(userId)

    fun getDefaultAddress(userId: String): LiveData<Address?> = addressDao.getDefaultAddress(userId)

    suspend fun addAddress(address: Address): Resource<Long> {
        return try {
            val count = addressDao.getAddressCount(address.userId)
            if (count >= Address.MAX_ADDRESSES) {
                Resource.Error("Maximum address limit reached")
            } else {
                val id = addressDao.insertAddress(address)
                if (count == 0) {
                    // Set as default if it's the first address
                    addressDao.setAsDefault(address.userId, id)
                }
                Resource.Success(id)
            }
        } catch (e: Exception) {
            Resource.Error("Failed to add address: ${e.message}")
        }
    }

    suspend fun updateAddress(address: Address): Resource<Unit> {
        return try {
            addressDao.updateAddress(address)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to update address: ${e.message}")
        }
    }

    suspend fun deleteAddress(address: Address): Resource<Unit> {
        return try {
            addressDao.deleteAddress(address)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to delete address: ${e.message}")
        }
    }

    suspend fun setDefaultAddress(userId: String, addressId: Long): Resource<Unit> {
        return try {
            addressDao.setAsDefault(userId, addressId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to set default address: ${e.message}")
        }
    }

    // Google Places API Operations
    fun getPlacePredictions(query: String): Flow<Resource<List<Place.AutocompletePrediction>>> = flow {
        try {
            emit(Resource.Loading())
            
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build()

            val response = placesClient.findAutocompletePredictions(request).await()
            emit(Resource.Success(response.autocompletePredictions))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to get place predictions: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getPlaceDetails(placeId: String): Resource<Place> {
        return try {
            val placeFields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS_COMPONENTS
            )
            
            val request = FetchPlaceRequest.builder(placeId, placeFields).build()
            val response = placesClient.fetchPlace(request).await()
            Resource.Success(response.place)
        } catch (e: Exception) {
            Resource.Error("Failed to get place details: ${e.message}")
        }
    }

    // Geocoding Operations
    suspend fun getAddressFromLocation(latLng: LatLng): Resource<Address> {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            if (addresses.isNullOrEmpty()) {
                Resource.Error("No address found for this location")
            } else {
                val address = addresses[0]
                Resource.Success(
                    Address(
                        addressLine1 = address.getAddressLine(0) ?: "",
                        city = address.locality ?: "",
                        state = address.adminArea ?: "",
                        zipCode = address.postalCode ?: "",
                        country = address.countryName ?: "",
                        latitude = latLng.latitude,
                        longitude = latLng.longitude
                    )
                )
            }
        } catch (e: IOException) {
            Resource.Error("Failed to get address from location: ${e.message}")
        }
    }

    suspend fun getLocationFromAddress(address: String): Resource<LatLng> {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(address, 1)

            if (addresses.isNullOrEmpty()) {
                Resource.Error("No location found for this address")
            } else {
                val location = addresses[0]
                Resource.Success(LatLng(location.latitude, location.longitude))
            }
        } catch (e: IOException) {
            Resource.Error("Failed to get location from address: ${e.message}")
        }
    }
}
