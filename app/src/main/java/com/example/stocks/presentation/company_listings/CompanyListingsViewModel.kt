package com.example.stocks.presentation.company_listings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stocks.domain.repository.StockRepository
import com.example.stocks.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CompanyListingsViewModel @Inject constructor(
    private val stockRepository: StockRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CompanyListingsState())
    val state = _state.asStateFlow()
    private var search: Job? = null

    init {
        getCompanyListings()
    }

    fun onEvent(event: CompanyListingsEvent) {
        when (event) {
            is CompanyListingsEvent.Refresh -> {
                getCompanyListings(fetchFromRemote = true)
            }

            is CompanyListingsEvent.OnSearchQueryChange -> {
                _state.value = _state.value.copy(searchQuery = event.query)
                search?.cancel()
                search = viewModelScope.launch {
                    delay(500L) // Debounce time for search query to avoid unnecessary api calls when user is typing
                    getCompanyListings()
                }
            }
        }
    }

    private fun getCompanyListings(
        query: String = _state.value.searchQuery.lowercase(),
        fetchFromRemote: Boolean = false
    ) {
        viewModelScope.launch {
            stockRepository.getCompanyListings(fetchFromRemote, query).collect { result ->
                withContext(Dispatchers.Main) {
                    when (result) {
                        is Resource.Success -> {
                            result.data?.let { listings ->
                                _state.value = _state.value.copy(
                                    companies = listings
                                )
                            }
                        }

                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                error = result.message
                            )
                        }

                        is Resource.Loading -> {
                            _state.value = _state.value.copy(
                                isLoading = result.isLoading
                            )
                        }
                    }
                }
            }
        }
    }
}