package com.example.stocks.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StockDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanyListings(
        companyListingEntities: List<CompanyListingEntity>
    )

    @Query("DELETE FROM companylistingentity")
    suspend fun clearCompanyListings()

    @Query(
        """
        SELECT * 
        FROM companylistingentity
        WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%' OR
            UPPER(:query) == symbol
        """
    )
    suspend fun searchCompanyListings(query: String): List<CompanyListingEntity>

    @Query("SELECT * FROM intradayinfoentity WHERE symbol = :symbol")
    suspend fun getIntradayInfos(symbol: String): List<IntradayInfoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntradayInfos(intradayInfos: List<IntradayInfoEntity>)

    @Query("DELETE FROM intradayinfoentity WHERE symbol = :symbol")
    suspend fun clearIntradayInfos(symbol: String)

    @Query("SELECT * FROM companyinfoentity WHERE symbol = :symbol")
    suspend fun getCompanyInfo(symbol: String): CompanyInfoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanyInfo(companyInfo: CompanyInfoEntity)

    @Query("DELETE FROM companyinfoentity WHERE symbol = :symbol")
    suspend fun clearCompanyInfo(symbol: String)
}