package com.example.stocks.presentation.company_listings

import com.example.stocks.domain.model.CompanyListing

data class CompanyListingsState(
    val companies: List<CompanyListing> = emptyList(),
    val isLoading: Boolean = false,
    var isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val error: String? = null
)