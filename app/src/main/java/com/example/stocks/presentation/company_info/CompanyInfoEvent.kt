package com.example.stocks.presentation.company_info

sealed class CompanyInfoEvent {
    data object Refresh: CompanyInfoEvent()
    data object RefreshCompanyInfo: CompanyInfoEvent()
    data object RefreshIntradayInfo: CompanyInfoEvent()
}