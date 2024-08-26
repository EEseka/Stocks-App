package com.example.stocks.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CompanyListingEntity::class, IntradayInfoEntity::class, CompanyInfoEntity::class],
    version = 4
)
abstract class StockDataBase : RoomDatabase() {
    abstract val dao: StockDao
}