package com.example.myshop.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myshop.models.Address

@Dao
interface AddressDao {
    @Query("SELECT * FROM addresses WHERE userId = :userId ORDER BY isDefault DESC, timestamp DESC")
    fun getAddresses(userId: String): LiveData<List<Address>>

    @Query("SELECT * FROM addresses WHERE userId = :userId AND isDefault = 1 LIMIT 1")
    fun getDefaultAddress(userId: String): LiveData<Address?>

    @Query("SELECT COUNT(*) FROM addresses WHERE userId = :userId")
    suspend fun getAddressCount(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: Address): Long

    @Update
    suspend fun updateAddress(address: Address)

    @Delete
    suspend fun deleteAddress(address: Address)

    @Query("UPDATE addresses SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefaultAddress(userId: String)

    @Query("UPDATE addresses SET isDefault = 1 WHERE id = :addressId")
    suspend fun setDefaultAddress(addressId: Long)

    @Transaction
    suspend fun setAsDefault(userId: String, addressId: Long) {
        clearDefaultAddress(userId)
        setDefaultAddress(addressId)
    }

    @Query("SELECT * FROM addresses WHERE id = :addressId")
    suspend fun getAddressById(addressId: Long): Address?

    @Query("DELETE FROM addresses WHERE userId = :userId")
    suspend fun deleteAllAddresses(userId: String)

    @Query("SELECT * FROM addresses WHERE userId = :userId AND zipCode = :zipCode LIMIT 1")
    suspend fun getAddressByZipCode(userId: String, zipCode: String): Address?

    @Query("SELECT * FROM addresses WHERE userId = :userId AND placeId = :placeId LIMIT 1")
    suspend fun getAddressByPlaceId(userId: String, placeId: String): Address?
}
