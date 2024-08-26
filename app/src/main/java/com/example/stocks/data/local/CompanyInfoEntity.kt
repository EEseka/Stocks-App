package com.example.stocks.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CompanyInfoEntity(
    val symbol: String,
    val description: String,
    val name: String,
    val country: String,
    val industry: String,
    val localTimestamp: Long,
    @PrimaryKey val id: Int? = null
)
