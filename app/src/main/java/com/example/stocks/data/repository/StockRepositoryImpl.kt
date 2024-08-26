package com.example.stocks.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.stocks.data.csv.CSVParser
import com.example.stocks.data.local.StockDataBase
import com.example.stocks.data.mapper.toCompanyInfo
import com.example.stocks.data.mapper.toCompanyInfoEntity
import com.example.stocks.data.mapper.toCompanyListing
import com.example.stocks.data.mapper.toCompanyListingEntity
import com.example.stocks.data.mapper.toIntradayInfo
import com.example.stocks.data.mapper.toIntradayInfoEntity
import com.example.stocks.data.remote.StockApi
import com.example.stocks.domain.model.CompanyInfo
import com.example.stocks.domain.model.CompanyListing
import com.example.stocks.domain.model.IntradayInfo
import com.example.stocks.domain.repository.StockRepository
import com.example.stocks.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okio.IOException
import retrofit2.HttpException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val api: StockApi,
    private val db: StockDataBase,
    private val companyListingsParser: CSVParser<CompanyListing>,
    private val intradayInfosParser: CSVParser<IntradayInfo>
) : StockRepository {
    private val dao = db.dao
    override suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>> {
        return withContext(Dispatchers.IO) {
            flow {
                emit(Resource.Loading(isLoading = true))
                val localListings = dao.searchCompanyListings(query)
                emit(Resource.Success(localListings.map { it.toCompanyListing() }))

                val isDbEmpty = localListings.isEmpty() && query.isBlank()

                val isDbValid = if (localListings.isNotEmpty()) {
                    !isCacheExpired(localListings.first().localTimestamp)
                } else null
                val shouldJustLoadFromCache = if (isDbValid == null) {
                    !isDbEmpty && !fetchFromRemote
                } else {
                    isDbValid && !fetchFromRemote
                }

                if (shouldJustLoadFromCache) {
                    emit(Resource.Loading(isLoading = false))
                    return@flow
                }

                try {
                    val remoteListings = withTimeout(10000L) {
                        val response = api.getListings()
                        companyListingsParser.parse(response.byteStream())
                    }
                    remoteListings.let { listings ->
                        dao.clearCompanyListings()
                        dao.insertCompanyListings(listings.map { it.toCompanyListingEntity(System.currentTimeMillis()) })
                        emit(
                            Resource.Success(
                                data = dao.searchCompanyListings("").map { it.toCompanyListing() })
                        )
                    }
                } catch (e: TimeoutCancellationException) {
                    emit(Resource.Error("Request timed out. Please try again."))
                } catch (e: IOException) {
                    e.printStackTrace()
                    emit(Resource.Error("Couldn't load data"))
                } catch (e: HttpException) {
                    e.printStackTrace()
                    emit(Resource.Error("Couldn't load data"))
                } finally {
                    emit(Resource.Loading(isLoading = false))
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getIntradayInfo(
        fetchFromRemote: Boolean,
        symbol: String
    ): Flow<Resource<List<IntradayInfo>>> {
        return withContext(Dispatchers.IO) {
            flow {
                emit(Resource.Loading(isLoading = true))
                val localListings = dao.getIntradayInfos(symbol)
                emit(Resource.Success(localListings.map { it.toIntradayInfo() }))

                val isDbValid = if (localListings.isNotEmpty()) {
                    !isCacheExpired(localListings.first().localTimestamp)
                } else {
                    false
                }
                val shouldJustLoadFromCache = isDbValid && !fetchFromRemote


                if (shouldJustLoadFromCache) {
                    emit(Resource.Loading(isLoading = false))
                    return@flow
                }

                try {
                    val remoteListings = withTimeout(10000L) {
                        val response = api.getIntradayInfo(symbol)
                        intradayInfosParser.parse(response.byteStream())
                    }

                    remoteListings.let { intradayListings ->
                        dao.clearIntradayInfos(symbol)
                        dao.insertIntradayInfos(intradayListings.map {
                            it.toIntradayInfoEntity(
                                symbol = symbol,
                                localTimestamp = System.currentTimeMillis()
                            )
                        })
                        emit(
                            Resource.Success(
                                dao.getIntradayInfos(symbol).map { it.toIntradayInfo() })
                        )
                    }
                } catch (e: TimeoutCancellationException) {
                    emit(Resource.Error(message = "Request timed out. Please try again."))
                } catch (e: IOException) {
                    e.printStackTrace()
                    emit(Resource.Error(message = "Couldn't load intraday info with symbol $symbol"))
                } catch (e: HttpException) {
                    e.printStackTrace()
                    emit(Resource.Error(message = "Couldn't load intraday info with symbol $symbol"))
                } finally {
                    emit(Resource.Loading(isLoading = false))
                }
            }
        }
    }

    override suspend fun getCompanyInfo(
        fetchFromRemote: Boolean,
        symbol: String
    ): Flow<Resource<CompanyInfo?>> {
        return withContext(Dispatchers.IO) {
            flow {
                emit(Resource.Loading(isLoading = true))
                val localInfo = dao.getCompanyInfo(symbol)
                emit(Resource.Success(localInfo?.toCompanyInfo()))

                val isDbEmpty = localInfo == null
                val isDbValid = !isDbEmpty && !isCacheExpired(localInfo!!.localTimestamp)
                val shouldJustLoadFromCache = isDbValid && !fetchFromRemote

                if (shouldJustLoadFromCache) {
                    emit(Resource.Loading(isLoading = false))
                    return@flow
                }

                try {
                    val remoteInfo = withTimeout(10000L) {
                        api.getCompanyInfo(symbol)
                    }
                    remoteInfo.let { result ->
                        dao.clearCompanyInfo(symbol)
                        dao.insertCompanyInfo(result.toCompanyInfoEntity(System.currentTimeMillis()))
                        emit(Resource.Success(dao.getCompanyInfo(symbol)?.toCompanyInfo()))
                    }
                } catch (e: TimeoutCancellationException) {
                    emit(Resource.Error(message = "Request timed out. Please try again."))
                } catch (e: IOException) {
                    e.printStackTrace()
                    emit(Resource.Error(message = "Couldn't load company info with symbol $symbol"))

                } catch (e: HttpException) {
                    e.printStackTrace()
                    emit(Resource.Error(message = "Couldn't load company info with symbol $symbol"))

                } finally {
                    emit(Resource.Loading(isLoading = false))
                }
            }
        }
    }

    private fun isCacheExpired(timestamp: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        val diff = currentTime - timestamp
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        return hours >= 24
    }
}