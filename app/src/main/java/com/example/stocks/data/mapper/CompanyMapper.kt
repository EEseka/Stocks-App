package com.example.stocks.data.mapper

import com.example.stocks.data.local.CompanyInfoEntity
import com.example.stocks.data.local.CompanyListingEntity
import com.example.stocks.data.remote.dto.CompanyInfoDto
import com.example.stocks.domain.model.CompanyInfo
import com.example.stocks.domain.model.CompanyListing
import java.sql.Timestamp

fun CompanyListingEntity.toCompanyListing(): CompanyListing {
    return CompanyListing(
        name = name,
        symbol = symbol,
        exchange = exchange
    )
}

fun CompanyListing.toCompanyListingEntity(localTimestamp: Long): CompanyListingEntity {
    return CompanyListingEntity(
        name = name,
        symbol = symbol,
        exchange = exchange,
        localTimestamp = localTimestamp
    )
}

fun CompanyInfoDto.toCompanyInfoEntity(localTimestamp: Long): CompanyInfoEntity {
    return CompanyInfoEntity(
        symbol = symbol ?: "Symbol not found",
        description = description ?: "Description not found",
        name = name ?: "Name not found",
        country = country ?: "Country not found",
        industry = industry ?: "Industry not found",
        localTimestamp = localTimestamp
    )
    // We make these null checks because we are using a free API.
    // Once we surpass our API call limit we will get a json file telling us that its finished.
    // If we don't make it null then our app will crash.
}

fun CompanyInfoEntity.toCompanyInfo(): CompanyInfo {
    return CompanyInfo(
        symbol = symbol,
        description = description,
        name = name,
        country = country,
        industry = industry
    )
}