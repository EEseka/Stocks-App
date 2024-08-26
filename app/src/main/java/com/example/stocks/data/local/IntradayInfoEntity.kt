package com.example.stocks.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class IntradayInfoEntity(
    val symbol: String,
    val timeStamp: String,
    val close: Double,
    val localTimestamp: Long,
    @PrimaryKey val id: Int? = null
)

