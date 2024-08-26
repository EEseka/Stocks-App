package com.example.stocks.presentation.company_info

import com.example.stocks.domain.model.CompanyInfo
import com.example.stocks.domain.model.IntradayInfo

data class CompanyInfoState(
    val stockInfo: List<IntradayInfo> = emptyList(),
    val company: CompanyInfo? = null,
    val isIntradayLoading: Boolean = false,
    val isCompanyInfoLoading: Boolean = false,
    var isRefreshing: Boolean = false,
    val intradayError: String? = null,
    val companyInfoError: String? = null
)