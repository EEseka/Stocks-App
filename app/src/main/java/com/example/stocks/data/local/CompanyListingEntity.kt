package com.example.stocks.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CompanyListingEntity(
    val name: String,
    val symbol: String,
    val exchange: String,
    val localTimestamp: Long,
    @PrimaryKey val id: Int? = null
)
