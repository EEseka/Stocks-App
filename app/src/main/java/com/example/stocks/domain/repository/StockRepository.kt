package com.example.stocks.domain.repository

import com.example.stocks.domain.model.CompanyInfo
import com.example.stocks.domain.model.CompanyListing
import com.example.stocks.domain.model.IntradayInfo
import com.example.stocks.util.Resource
import kotlinx.coroutines.flow.Flow

interface StockRepository {

    suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>>

    suspend fun getCompanyInfo(
        fetchFromRemote: Boolean,
        symbol: String
    ): Flow<Resource<CompanyInfo?>>

    suspend fun getIntradayInfo(
        fetchFromRemote: Boolean,
        symbol: String,
    ): Flow<Resource<List<IntradayInfo>>>
}