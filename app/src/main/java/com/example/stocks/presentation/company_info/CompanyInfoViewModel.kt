package com.example.stocks.presentation.company_info

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stocks.domain.repository.StockRepository
import com.example.stocks.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CompanyInfoViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: StockRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CompanyInfoState())
    val state = _state.asStateFlow()

    init {
        getIntradayInfos()
        getCompanyInfo()
    }

    fun onEvent(event: CompanyInfoEvent) {
        when (event) {
            CompanyInfoEvent.Refresh -> {
                getIntradayInfos(true)
                getCompanyInfo(true)
            }

            CompanyInfoEvent.RefreshCompanyInfo -> {
                getCompanyInfo(true)
            }

            CompanyInfoEvent.RefreshIntradayInfo -> {
                getIntradayInfos(true)
            }
        }
    }

    private fun getIntradayInfos(
        fetchFromRemote: Boolean = false,
        symbol: String = savedStateHandle.get<String>("symbol") ?: ""
    ) {
        viewModelScope.launch {
            repository.getIntradayInfo(fetchFromRemote, symbol).collect { result ->
                withContext(Dispatchers.Main) {
                    when (result) {
                        is Resource.Error -> {
                            _state.value = _state.value.copy(intradayError = result.message)
                        }

                        is Resource.Loading -> {
                            _state.value = _state.value.copy(isIntradayLoading = result.isLoading)
                        }

                        is Resource.Success -> {
                            result.data?.let {
                                _state.value = _state.value.copy(stockInfo = it)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getCompanyInfo(
        fetchFromRemote: Boolean = false,
        symbol: String = savedStateHandle.get<String>("symbol") ?: ""
    ) {
        viewModelScope.launch {
            repository.getCompanyInfo(fetchFromRemote, symbol).collect { result ->
                withContext(Dispatchers.Main) {
                    when (result) {
                        is Resource.Error -> {
                            _state.value = _state.value.copy(companyInfoError = result.message)
                        }

                        is Resource.Loading -> {
                            _state.value =
                                _state.value.copy(isCompanyInfoLoading = result.isLoading)
                        }

                        is Resource.Success -> {
                            result.data?.let {
                                _state.value = _state.value.copy(company = it)
                            }
                        }
                    }
                }
            }
        }
    }
}