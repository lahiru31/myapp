package com.example.myshop.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "addresses")
data class Address(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "",
    val name: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val country: String = "",
    val phoneNumber: String = "",
    val isDefault: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val placeId: String = "",
    val formattedAddress: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Serializable {
    
    fun getFullAddress(): String {
        return buildString {
            append(addressLine1)
            if (addressLine2.isNotEmpty()) {
                append("\n")
                append(addressLine2)
            }
            append("\n")
            append(city)
            append(", ")
            append(state)
            append(" ")
            append(zipCode)
            append("\n")
            append(country)
        }
    }

    fun getShortAddress(): String {
        return "$addressLine1, $city"
    }

    companion object {
        const val MAX_ADDRESSES = 5
    }
}
